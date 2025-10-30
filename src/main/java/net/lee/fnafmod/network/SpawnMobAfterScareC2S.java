// java
package net.lee.fnafmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class SpawnMobAfterScareC2S {
    private static final int MAX_UTF_LENGTH = 32767;

    private final String mobId;
    private final int offX, offY, offZ;

    public SpawnMobAfterScareC2S(String mobId, int offX, int offY, int offZ) {
        this.mobId = mobId;
        this.offX = offX;
        this.offY = offY;
        this.offZ = offZ;
    }

    public static void encode(SpawnMobAfterScareC2S msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.mobId, MAX_UTF_LENGTH);
        buf.writeVarInt(msg.offX);
        buf.writeVarInt(msg.offY);
        buf.writeVarInt(msg.offZ);
    }

    public static SpawnMobAfterScareC2S decode(FriendlyByteBuf buf) {
        String id = buf.readUtf(MAX_UTF_LENGTH);
        int x = buf.readVarInt();
        int y = buf.readVarInt();
        int z = buf.readVarInt();
        return new SpawnMobAfterScareC2S(id, x, y, z);
    }

    public static void handle(SpawnMobAfterScareC2S msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();

        ServerPlayer sender = ctx.getSender();
        if (sender == null || !sender.isAlive() || sender.isSpectator()) {
            ctx.setPacketHandled(true);
            return;
        }

        final String mobId = msg.mobId;
        final int offX = msg.offX;
        final int offY = msg.offY;
        final int offZ = msg.offZ;

        ctx.enqueueWork(() -> {
            ServerLevel level = sender.serverLevel();
            if (level == null || sender.isSleeping() || sender.connection == null) return;

            net.minecraft.world.entity.EntityType.byString(mobId).ifPresent(type -> {
                var entity = type.create(level);
                if (!(entity instanceof net.minecraft.world.entity.Mob mob)) return;

                var pos = sender.blockPosition().offset(offX, offY, offZ);
                mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                        level.random.nextFloat() * 360f, 0f);
                level.addFreshEntity(mob);
            });
        });

        ctx.setPacketHandled(true);
    }
}
