package lee.fnafmod.platform;

public class ClientEventHandlerImpl {
    public static ClientEventHandler INSTANCE;

    public static void setInstance(ClientEventHandler handler) {
        INSTANCE = handler;
    }


}
