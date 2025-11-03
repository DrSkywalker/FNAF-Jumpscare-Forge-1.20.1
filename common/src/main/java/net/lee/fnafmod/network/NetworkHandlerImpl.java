package net.lee.fnafmod.network;

import net.minecraft.resources.ResourceLocation;

/**
 * Platform-specific implementation holder
 */
public class NetworkHandlerImpl {
    public static NetworkHandler INSTANCE;
    
    public static void setInstance(NetworkHandler handler) {
        INSTANCE = handler;
    }
}
