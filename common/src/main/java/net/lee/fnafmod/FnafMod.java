package net.lee.fnafmod;

import net.lee.fnafmod.client.Keybinds;
import net.lee.fnafmod.platform.ClientEventHandler;

public final class FnafMod {
    public static final String MOD_ID = "fnafmod";

    public static void init() {
        // Common initialization code
    }
    
    public static void initClient() {
        // Initialize keybindings
        Keybinds.init();
        
        // Register platform-specific client events
        if (ClientEventHandler.getInstance() != null) {
            ClientEventHandler.getInstance().registerClientEvents();
            ClientEventHandler.getInstance().registerKeyBindings();
            ClientEventHandler.getInstance().registerRenderOverlay();
        }
    }
}
