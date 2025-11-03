package net.lee.fnafmod.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.lee.fnafmod.FnafMod;
import net.lee.fnafmod.client.JumpscareManager;
import net.lee.fnafmod.client.Keybinds;
import net.lee.fnafmod.fabric.client.FabricClientEventHandler;
import net.lee.fnafmod.platform.ClientEventHandlerImpl;
import org.lwjgl.glfw.GLFW;

public final class FnafModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Set up platform-specific client event handler
        ClientEventHandlerImpl.setInstance(new FabricClientEventHandler());
        
        // Run common client setup
        FnafMod.initClient();
    }
}
