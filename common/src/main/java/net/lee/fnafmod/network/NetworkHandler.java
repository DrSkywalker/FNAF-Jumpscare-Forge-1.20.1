package net.lee.fnafmod.network;

import net.minecraft.resources.ResourceLocation;

/**
 * Platform-agnostic interface for sending spawn mob packets
 */
public interface NetworkHandler {
    void sendSpawnMobPacket(ResourceLocation mobId, String spawnName, int offX, int offY, int offZ, String[] armor);
    
    static NetworkHandler getInstance() {
        return NetworkHandlerImpl.INSTANCE;
    }
}
