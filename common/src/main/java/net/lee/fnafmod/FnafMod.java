package net.lee.fnafmod;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.lee.fnafmod.client.JumpscareManager;
import net.lee.fnafmod.client.Keybinds;
import net.lee.fnafmod.network.FnafNet;
import org.lwjgl.glfw.GLFW;

public class FnafMod {
    public static final String MOD_ID = "fnafmod";

    public static void init() {
        FnafNet.register();
    }

    public static void initClient() {
        // Register keybinds
        Keybinds.register();

        // Register client tick event
        ClientTickEvent.CLIENT_POST.register(client -> {
            if (Keybinds.TEST_SCARE.consumeClick()) {
                JumpscareManager.get().triggerRandom();
            }
            JumpscareManager.get().tick();
        });

        // Register key input event
        ClientRawInputEvent.KEY_PRESSED.register((client, keyCode, scanCode, action, modifiers) -> {
            if (action == GLFW.GLFW_PRESS
                    && Keybinds.TEST_SCARE.matches(keyCode, scanCode)
                    && !JumpscareManager.get().isActive()) {
                JumpscareManager.get().triggerRandom();
            }
            return EventResult.pass();
        });

        // Register screen render event
        ClientGuiEvent.RENDER_POST.register((screen, graphics, mouseX, mouseY, tickDelta) -> {
            JumpscareManager.get().render(graphics);
        });
    }
}
