package net.lee.fnafmod.network;

import net.lee.fnafmod.FnafModClient;
import net.lee.fnafmod.util.ArmorRandomizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public record SpawnMobAfterScarePayload(
        Identifier mobId,
        String spawnName,
        int offX,
        int offY,
        int offZ,
        String[] armor
) implements CustomPayload {
    
    public static final CustomPayload.Id<SpawnMobAfterScarePayload> ID = 
            new CustomPayload.Id<>(Identifier.of(FnafModClient.MOD_ID, "spawn_mob"));

    public static final PacketCodec<RegistryByteBuf, SpawnMobAfterScarePayload> CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC, SpawnMobAfterScarePayload::mobId,
            PacketCodecs.STRING.collect(PacketCodecs.toNullable()), SpawnMobAfterScarePayload::spawnName,
            PacketCodecs.INTEGER, SpawnMobAfterScarePayload::offX,
            PacketCodecs.INTEGER, SpawnMobAfterScarePayload::offY,
            PacketCodecs.INTEGER, SpawnMobAfterScarePayload::offZ,
            PacketCodecs.STRING.collect(PacketCodecs.toList()).xmap(
                    list -> list.toArray(new String[0]),
                    java.util.Arrays::asList
            ),
            SpawnMobAfterScarePayload::armor,
            SpawnMobAfterScarePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void handle(SpawnMobAfterScarePayload payload, ServerPlayerEntity player) {
        player.server.execute(() -> {
            if (player == null) return;
            var world = player.getWorld();
            var entityType = Registries.ENTITY_TYPE.get(payload.mobId);
            if (entityType == null) return;

            Entity e = entityType.create(world);
            if (e instanceof MobEntity mob) {
                mob.refreshPositionAndAngles(
                        player.getX() + payload.offX,
                        player.getY() + payload.offY,
                        player.getZ() + payload.offZ,
                        player.getYaw(),
                        player.getPitch()
                );

                // set custom name if provided
                if (payload.spawnName != null) {
                    mob.setCustomName(Text.literal(payload.spawnName));
                    mob.setCustomNameVisible(true);
                }

                EquipmentSlot[] slots = new EquipmentSlot[]{
                        EquipmentSlot.HEAD,
                        EquipmentSlot.CHEST,
                        EquipmentSlot.LEGS,
                        EquipmentSlot.FEET
                };
                Random rnd = mob.getRandom();

                if (payload.armor == null) {
                    ArmorRandomizer.equipRandomArmor(mob, rnd, false);
                } else {
                    for (int i = 0; i < 4; i++) {
                        String id = payload.armor.length > i ? payload.armor[i] : null;
                        if (id == null) continue;
                        if ("random".equalsIgnoreCase(id)) {
                            Item item = ArmorRandomizer.pickArmorForSlot(rnd, slots[i]);
                            if (item != Items.AIR) mob.equipStack(slots[i], new ItemStack(item));
                            continue;
                        }
                        Identifier itemId = Identifier.tryParse(id);
                        Item item = itemId != null ? Registries.ITEM.get(itemId) : Items.AIR;
                        if (item != Items.AIR) {
                            mob.equipStack(slots[i], new ItemStack(item));
                        }
                    }
                }

                world.spawnEntity(mob);
            }
        });
    }
}
