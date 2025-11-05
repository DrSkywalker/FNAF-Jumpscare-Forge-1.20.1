package lee.fnafmod.network;

import net.minecraft.resources.ResourceLocation;

public record SpawnMobPacket(
        ResourceLocation mobId,
        String spawnName,
        int offX,
        int offY,
        int offZ,
        String[] armor
) {
}