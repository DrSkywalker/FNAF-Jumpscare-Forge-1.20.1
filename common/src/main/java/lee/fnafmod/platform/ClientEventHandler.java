package lee.fnafmod.platform;

public interface ClientEventHandler {

    static ClientEventHandler getInstance() {
        return ClientEventHandlerImpl.INSTANCE;
    }

    void registerClientEvents();

    void registerKeyBindings();

    void registerRenderOverlay();
}
