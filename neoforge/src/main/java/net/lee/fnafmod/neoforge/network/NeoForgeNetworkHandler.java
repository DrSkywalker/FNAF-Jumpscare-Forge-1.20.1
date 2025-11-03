package net.lee.fnafmod.neoforge.network;

import io.netty.buffer.ByteBuf;
import net.lee.fnafmod.FnafMod;
import net.lee.fnafmod.network.NetworkHandler;
import net.lee.fnafmod.network.SpawnMobPacket;
import net.lee.fnafmod.util.ArmorRandomizer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NeoForgeNetworkHandler implements NetworkHandler {
    
    public static final CustomPacketPayload.Type<SpawnMobPayload> SPAWN_MOB_TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FnafMod.MOD_ID, "spawn_mob"));
    
    public NeoForgeNetworkHandler() {
        // Registration will be done via the registrar when it's available
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
        PacketDistributor.sendToServer(payload);
    }
    
    public record SpawnMobPayload(SpawnMobPacket packet) implements CustomPacketPayload {
        
        public static final StreamCodec<ByteBuf, SpawnMobPayload> STREAM_CODEC = 
            StreamCodec.composite(
                ResourceLocation.STREAM_CODEC,
                p -> p.packet.mobId(),
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
                p -> java.util.Optional.ofNullable(p.packet.spawnName()),
                ByteBufCodecs.INT,
                p -> p.packet.offX(),
                ByteBufCodecs.INT,
                p -> p.packet.offY(),
                ByteBufCodecs.INT,
                p -> p.packet.offZ(),
                ByteBufCodecs.collection(java.util.ArrayList::new, ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), 4),
                p -> {
                    java.util.List<java.util.Optional<String>> list = new java.util.ArrayList<>();
                    if (p.packet.armor() != null) {
                        for (String s : p.packet.armor()) {
                            list.add(java.util.Optional.ofNullable(s));
                        }
                    }
                    while (list.size() < 4) list.add(java.util.Optional.empty());
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
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return SPAWN_MOB_TYPE;
        }
        
        public static void handle(SpawnMobPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                ServerPlayer sender = (ServerPlayer) context.player();
                if (sender == null) return;
                SpawnMobPacket msg = payload.packet;
                Level level = sender.level();
                var reg = level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE);
                reg.getOptional(msg.mobId()).ifPresent(type -> {
                    Entity e = type.create(level);
                    if (e instanceof Mob mob) {
                        mob.moveTo(sender.getX() + msg.offX(), sender.getY() + msg.offY(), sender.getZ() + msg.offZ(), 
                                   sender.getYRot(), sender.getXRot());
                        
                        // set custom name if provided
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
                                var itemRegistry = level.registryAccess().registryOrThrow(Registries.ITEM);
                                Item item = itemRegistry.getOptional(ResourceLocation.tryParse(id))
                                    .orElse(Items.AIR);
                                if (item != Items.AIR) {
                                    mob.setItemSlot(slots[i], new ItemStack(item));
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
