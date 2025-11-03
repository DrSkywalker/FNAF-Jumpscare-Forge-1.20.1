package net.lee.fnafmod.network;

import dev.architectury.networking.NetworkManager;
import net.lee.fnafmod.FnafMod;
import net.minecraft.resources.ResourceLocation;

public class FnafNet {
    public static final ResourceLocation SPAWN_MOB_PACKET = ResourceLocation.fromNamespaceAndPath(FnafMod.MOD_ID, "spawn_mob");

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SPAWN_MOB_PACKET, SpawnMobAfterScareC2S::handle);
    }
}
