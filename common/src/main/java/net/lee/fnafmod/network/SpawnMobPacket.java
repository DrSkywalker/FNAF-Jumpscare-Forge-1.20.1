package net.lee.fnafmod.network;

import net.minecraft.resources.ResourceLocation;

/**
 * Common packet data for spawning mobs
 */
public record SpawnMobPacket(
    ResourceLocation mobId, 
    String spawnName, 
    int offX, 
    int offY, 
    int offZ,
    String[] armor
) {
}
