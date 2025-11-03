package net.lee.fnafmod.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lee.fnafmod.FnafMod;
import net.lee.fnafmod.network.NetworkHandler;
import net.lee.fnafmod.network.SpawnMobPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class FabricNetworkHandler implements NetworkHandler {
    
    public static final CustomPacketPayload.Type<SpawnMobPayload> SPAWN_MOB_TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FnafMod.MOD_ID, "spawn_mob"));
    
    public FabricNetworkHandler() {
        // Register the packet type
        PayloadTypeRegistry.playC2S().register(SPAWN_MOB_TYPE, SpawnMobPayload.STREAM_CODEC);
        
        // Register server-side handler
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
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return SPAWN_MOB_TYPE;
        }
        
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
        
        public static void handle(SpawnMobPayload payload, net.minecraft.server.level.ServerPlayer sender) {
            if (sender == null) return;
            SpawnMobPacket msg = payload.packet;
            sender.server.execute(() -> {
                net.minecraft.world.level.Level level = sender.level();
                var reg = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENTITY_TYPE);
                reg.getOptional(msg.mobId()).ifPresent(type -> {
                    net.minecraft.world.entity.Entity e = type.create(level);
                    if (e instanceof net.minecraft.world.entity.Mob mob) {
                        mob.moveTo(sender.getX() + msg.offX(), sender.getY() + msg.offY(), sender.getZ() + msg.offZ(), 
                                   sender.getYRot(), sender.getXRot());
                        
                        // set custom name if provided
                        if (msg.spawnName() != null) {
                            mob.setCustomName(net.minecraft.network.chat.Component.literal(msg.spawnName()));
                            mob.setCustomNameVisible(true);
                        }
                        
                        net.minecraft.world.entity.EquipmentSlot[] slots = new net.minecraft.world.entity.EquipmentSlot[]{
                            net.minecraft.world.entity.EquipmentSlot.HEAD, 
                            net.minecraft.world.entity.EquipmentSlot.CHEST, 
                            net.minecraft.world.entity.EquipmentSlot.LEGS, 
                            net.minecraft.world.entity.EquipmentSlot.FEET
                        };
                        net.minecraft.util.RandomSource rnd = mob.getRandom();
                        
                        if (msg.armor() == null) {
                            net.lee.fnafmod.util.ArmorRandomizer.equipRandomArmor(mob, rnd, false);
                        } else {
                            for (int i = 0; i < 4; i++) {
                                String id = msg.armor().length > i ? msg.armor()[i] : null;
                                if (id == null) continue;
                                if ("random".equalsIgnoreCase(id)) {
                                    net.minecraft.world.item.Item item = net.lee.fnafmod.util.ArmorRandomizer.pickArmorForSlot(rnd, slots[i]);
                                    if (item != net.minecraft.world.item.Items.AIR) 
                                        mob.setItemSlot(slots[i], new net.minecraft.world.item.ItemStack(item));
                                    continue;
                                }
                                var itemRegistry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ITEM);
                                net.minecraft.world.item.Item item = itemRegistry.getOptional(ResourceLocation.tryParse(id))
                                    .orElse(net.minecraft.world.item.Items.AIR);
                                if (item != net.minecraft.world.item.Items.AIR) {
                                    mob.setItemSlot(slots[i], new net.minecraft.world.item.ItemStack(item));
                                }
                            }
                        }
                        
                        level.addFreshEntity(mob);
                    }
                });
            });
        }
    }
}
