package lee.fnafmod.neoforge.network;

import io.netty.buffer.ByteBuf;
import lee.fnafmod.network.NetworkHandler;
import lee.fnafmod.network.SpawnMobPacket;
import lee.fnafmod.util.ArmorRandomizer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NeoForgeNetworkHandler implements NetworkHandler {
    public static final CustomPacketPayload.Type<SpawnMobPayload> SPAWN_MOB_TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(lee.fnafmod.fnafmod.MOD_ID, "spawn_mob"));

    public NeoForgeNetworkHandler() {
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(
                SPAWN_MOB_TYPE,
                SpawnMobPayload.STREAM_CODEC,
                SpawnMobPayload::handle
        );
    }

    @Override
    public void sendSpawnMobPacket(ResourceLocation mobId, String spawnName, int offX, int offY, int offZ, String[] armor) {
        SpawnMobPayload payload = new SpawnMobPayload(
                new SpawnMobPacket(mobId, spawnName, offX, offY, offZ, armor)
        );
        ClientPacketDistributor.sendToServer(payload);
    }

    public record SpawnMobPayload(SpawnMobPacket packet) implements CustomPacketPayload {

        public static final StreamCodec<ByteBuf, SpawnMobPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC,
                        p -> p.packet.mobId(),
                        ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
                        p -> Optional.ofNullable(p.packet.spawnName()),
                        ByteBufCodecs.INT,
                        p -> p.packet.offX(),
                        ByteBufCodecs.INT,
                        p -> p.packet.offY(),
                        ByteBufCodecs.INT,
                        p -> p.packet.offZ(),
                        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), 4),
                        p -> {
                            List<Optional<String>> list = new ArrayList<>();
                            if (p.packet.armor() != null) {
                                for (String s : p.packet.armor()) {
                                    list.add(Optional.ofNullable(s));
                                }
                            }
                            while (list.size() < 4) list.add(Optional.empty());
                            return list;
                        },
                        (mobId, spawnName, offX, offY, offZ, armor) -> {
                            String[] armorArray = new String[4];
                            for (int i = 0; i < 4 && i < armor.size(); i++) {
                                armorArray[i] = armor.get(i).orElse(null);
                            }
                            return new SpawnMobPayload(
                                    new SpawnMobPacket(mobId, spawnName.orElse(null), offX, offY, offZ, armorArray)
                            );
                        }
                );

        public static void handle(SpawnMobPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                ServerPlayer sender = (ServerPlayer) context.player();
                if (sender == null) return;
                SpawnMobPacket msg = payload.packet;
                ServerLevel level = sender.level();
                Optional<Holder.Reference<EntityType<?>>> typeRefOpt = BuiltInRegistries.ENTITY_TYPE.get(msg.mobId());
                if (typeRefOpt.isEmpty()) return;

                EntityType<?> type = typeRefOpt.get().value();

                Vec3 base = sender.position();
                BlockPos spawnPos = BlockPos.containing(base).offset(msg.offX(), msg.offY(), msg.offZ());

                Entity spawned = type.spawn(level, spawnPos, EntitySpawnReason.TRIGGERED);
                if (spawned == null) return;

                if (spawned instanceof Mob mob) {
                    mob.setYRot(sender.getYRot());
                    mob.setXRot(sender.getXRot());

                    if (msg.spawnName() != null) {
                        mob.setCustomName(Component.literal(msg.spawnName()));
                        mob.setCustomNameVisible(true);
                    }

                    EquipmentSlot[] slots = new EquipmentSlot[]{
                            EquipmentSlot.HEAD,
                            EquipmentSlot.CHEST,
                            EquipmentSlot.LEGS,
                            EquipmentSlot.FEET
                    };
                    RandomSource rnd = mob.getRandom();

                    if (msg.armor() == null) {
                        ArmorRandomizer.equipRandomArmor(mob, rnd, false);
                    } else {
                        for (int i = 0; i < 4; i++) {
                            String id = msg.armor().length > i ? msg.armor()[i] : null;
                            if (id == null) continue;

                            if ("random".equalsIgnoreCase(id)) {
                                Item randomItem = ArmorRandomizer.pickArmorForSlot(rnd, slots[i]);
                                if (randomItem != Items.AIR) {
                                    mob.setItemSlot(slots[i], new ItemStack(randomItem));
                                }
                                continue;
                            }

                            ResourceLocation itemId = ResourceLocation.tryParse(id);
                            if (itemId == null) continue;

                            Optional<Holder.Reference<Item>> itemRefOpt = BuiltInRegistries.ITEM.get(itemId);
                            Item item = itemRefOpt.map(Holder::value).orElse(Items.AIR);
                            if (item != Items.AIR) {
                                mob.setItemSlot(slots[i], new ItemStack(item));
                            }
                        }
                    }
                }
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return SPAWN_MOB_TYPE;
        }
    }
}
