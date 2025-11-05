package lee.fnafmod.network;

public class NetworkHandlerImpl {

    public static NetworkHandler INSTANCE;

    public static void setInstance(NetworkHandler handler) {
        INSTANCE = handler;
    }
}
