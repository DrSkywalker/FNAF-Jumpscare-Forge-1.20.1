package net.lee.fnafmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lee.fnafmod.network.SpawnMobAfterScarePayload;

public class FnafMod implements ModInitializer {
    public static final String MOD_ID = "fnafmod";

    @Override
    public void onInitialize() {
        // Register the payload for server-side handling
        PayloadTypeRegistry.playC2S().register(SpawnMobAfterScarePayload.ID, SpawnMobAfterScarePayload.CODEC);
        
        // Register server-side packet handler
        ServerPlayNetworking.registerGlobalReceiver(SpawnMobAfterScarePayload.ID,
                (payload, context) -> SpawnMobAfterScarePayload.handle(payload, context.player()));
    }
}
