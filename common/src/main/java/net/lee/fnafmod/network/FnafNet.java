package net.lee.fnafmod.network;

import dev.architectury.networking.NetworkChannel;
import net.lee.fnafmod.FnafMod;
import net.minecraft.resources.ResourceLocation;

public class FnafNet {
    public static final NetworkChannel CHANNEL = NetworkChannel.create(
            ResourceLocation.fromNamespaceAndPath(FnafMod.MOD_ID, "main")
    );

    public static void register() {
        CHANNEL.register(SpawnMobAfterScareC2S.class,
                SpawnMobAfterScareC2S::encode,
                SpawnMobAfterScareC2S::decode,
                SpawnMobAfterScareC2S::handle
        );
    }
}
