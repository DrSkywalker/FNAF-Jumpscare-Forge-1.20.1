package net.lee.fnafmod.platform;

/**
 * Platform-agnostic interface for client event registration
 */
public interface ClientEventHandler {
    void registerClientEvents();
    void registerKeyBindings();
    void registerRenderOverlay();
    
    static ClientEventHandler getInstance() {
        return ClientEventHandlerImpl.INSTANCE;
    }
}
