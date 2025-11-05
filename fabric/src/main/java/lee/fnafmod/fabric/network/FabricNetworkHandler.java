package lee.fnafmod.fabric.network;

import lee.fnafmod.fnafmod;
import lee.fnafmod.network.NetworkHandler;
import lee.fnafmod.network.SpawnMobPacket;
import lee.fnafmod.util.ArmorRandomizer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class FabricNetworkHandler implements NetworkHandler {

    public static final CustomPacketPayload.Type<SpawnMobPayload> SPAWN_MOB_TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(fnafmod.MOD_ID, "spawn_mob"));

    public FabricNetworkHandler() {
        PayloadTypeRegistry.playC2S().register(SPAWN_MOB_TYPE, SpawnMobPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SPAWN_MOB_TYPE,
                (payload, context) -> SpawnMobPayload.handle(payload, context.player())
        );
    }

    @Override
    public void sendSpawnMobPacket(ResourceLocation mobId, String spawnName, int offX, int offY, int offZ, String[] armor) {
        SpawnMobPayload payload = new SpawnMobPayload(
                new SpawnMobPacket(mobId, spawnName, offX, offY, offZ, armor)
        );
        ClientPlayNetworking.send(payload);
    }

    public record SpawnMobPayload(SpawnMobPacket packet) implements CustomPacketPayload {

        public static final StreamCodec<FriendlyByteBuf, SpawnMobPayload> STREAM_CODEC =
                StreamCodec.of(SpawnMobPayload::write, SpawnMobPayload::read);

        public static void write(FriendlyByteBuf buf, SpawnMobPayload payload) {
            SpawnMobPacket packet = payload.packet;
            buf.writeResourceLocation(packet.mobId());
            buf.writeBoolean(packet.spawnName() != null);
            if (packet.spawnName() != null) buf.writeUtf(packet.spawnName());
            buf.writeInt(packet.offX());
            buf.writeInt(packet.offY());
            buf.writeInt(packet.offZ());
            for (int i = 0; i < 4; i++) {
                String s = (packet.armor() != null && i < packet.armor().length) ? packet.armor()[i] : null;
                buf.writeBoolean(s != null);
                if (s != null) buf.writeUtf(s);
            }
        }

        public static SpawnMobPayload read(FriendlyByteBuf buf) {
            ResourceLocation id = buf.readResourceLocation();
            String spawnName = buf.readBoolean() ? buf.readUtf(32767) : null;
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            String[] armor = new String[4];
            for (int i = 0; i < 4; i++) {
                boolean present = buf.readBoolean();
                armor[i] = present ? buf.readUtf(32767) : null;
            }
            return new SpawnMobPayload(new SpawnMobPacket(id, spawnName, x, y, z, armor));
        }

        public static void handle(SpawnMobPayload payload, ServerPlayer sender) {
            if (sender == null) return;
            SpawnMobPacket msg = payload.packet;

            Level level = sender.level();
            if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;

            MinecraftServer server = level.getServer();

            server.execute(() -> {
                Registry<EntityType<?>> entityTypeRegistry = level.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE);
                Optional<EntityType<?>> entityTypeOpt = entityTypeRegistry.getOptional(msg.mobId());

                Vec3 spawnPos = new Vec3(
                        sender.getX() + msg.offX(),
                        sender.getY() + msg.offY(),
                        sender.getZ() + msg.offZ()
                );

                entityTypeOpt.ifPresent(type -> {
                    BlockPos spawnBlockPos = BlockPos.containing(spawnPos);
                    Entity e = type.spawn(
                            serverLevel,
                            null,
                            null,
                            spawnBlockPos, // Pass the correct BlockPos instance
                            EntitySpawnReason.COMMAND,
                            true,
                            false
                    );

                    if (e instanceof Mob mob) {
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
                                    Item item = ArmorRandomizer.pickArmorForSlot(rnd, slots[i]);
                                    if (item != Items.AIR)
                                        mob.setItemSlot(slots[i], new ItemStack(item));
                                    continue;
                                }

                                Registry<Item> itemRegistry = level.registryAccess().lookupOrThrow(Registries.ITEM);
                                Item item = itemRegistry.getOptional(ResourceLocation.tryParse(id))
                                        .orElse(Items.AIR);
                                if (item != Items.AIR) {
                                    mob.setItemSlot(slots[i], new ItemStack(item));
                                }
                            }
                        }
                    }
                });
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return SPAWN_MOB_TYPE;
        }
    }
}
