package net.lee.fnafmod.platform;

/**
 * Platform-specific implementation holder
 */
public class ClientEventHandlerImpl {
    public static ClientEventHandler INSTANCE;
    
    public static void setInstance(ClientEventHandler handler) {
        INSTANCE = handler;
    }
}
