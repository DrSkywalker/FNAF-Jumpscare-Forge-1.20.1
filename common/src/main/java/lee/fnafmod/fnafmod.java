package lee.fnafmod;

import lee.fnafmod.client.Keybinds;
import lee.fnafmod.platform.ClientEventHandler;

public final class fnafmod {
    public static final String MOD_ID = "fnafmod";

    public static void init() {
    }

    public static void initClient() {
        Keybinds.init();
        if (ClientEventHandler.getInstance() != null) {
            ClientEventHandler.getInstance().registerClientEvents();
            ClientEventHandler.getInstance().registerKeyBindings();
            ClientEventHandler.getInstance().registerRenderOverlay();
        }
    }
}
