package net.lee.fnafmod.fabric.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.lee.fnafmod.client.JumpscareManager;
import net.lee.fnafmod.client.Keybinds;
import net.lee.fnafmod.platform.ClientEventHandler;
import org.lwjgl.glfw.GLFW;

public class FabricClientEventHandler implements ClientEventHandler {
    
    @Override
    public void registerClientEvents() {
        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Keybinds.TEST_SCARE != null && Keybinds.TEST_SCARE.consumeClick()) {
                JumpscareManager.get().triggerRandom();
            }
            JumpscareManager.get().tick();
        });
    }
    
    @Override
    public void registerKeyBindings() {
        // Register keybinding
        if (Keybinds.TEST_SCARE != null) {
            KeyBindingHelper.registerKeyBinding(Keybinds.TEST_SCARE);
        }
    }
    
    @Override
    public void registerRenderOverlay() {
        // Register HUD rendering
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            JumpscareManager.get().render(drawContext);
        });
    }
}
