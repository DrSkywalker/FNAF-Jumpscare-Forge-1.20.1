package lee.fnafmod.network;

import net.minecraft.resources.ResourceLocation;

public interface NetworkHandler {
    static NetworkHandler getInstance() {
        return NetworkHandlerImpl.INSTANCE;
    }

    void sendSpawnMobPacket(ResourceLocation mobId, String spawnName, int offX, int offY, int offZ, String[] armor);
}
