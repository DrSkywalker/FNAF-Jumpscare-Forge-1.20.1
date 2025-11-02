package net.lee.fnafmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.lee.fnafmod.client.JumpscareManager;
import net.lee.fnafmod.client.Keybinds;
import net.lee.fnafmod.network.SpawnMobAfterScarePayload;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class FnafModClient implements ClientModInitializer {
    public static final String MOD_ID = "fnafmod";

    @Override
    public void onInitializeClient() {
        // Register keybindings
        Keybinds.register();

        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Keybinds.TEST_SCARE.wasPressed()) {
                JumpscareManager.get().triggerRandom();
            }
            JumpscareManager.get().tick();
        });

        // Register HUD rendering
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            JumpscareManager.get().render(drawContext);
        });

        // Register screen render event for rendering on screens
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenEvents.afterRender(screen).register((scr, context, mouseX, mouseY, delta) -> {
                JumpscareManager.get().render(context);
            });
            
            // Register keyboard events for screens
            ScreenKeyboardEvents.afterKeyPress(screen).register((scr, key, scancode, modifiers) -> {
                if (key == GLFW.GLFW_KEY_F6 && Keybinds.TEST_SCARE.matchesKey(key, scancode) 
                        && !JumpscareManager.get().isActive()) {
                    JumpscareManager.get().triggerRandom();
                }
            });
        });

        // Register network payload
        PayloadTypeRegistry.playC2S().register(SpawnMobAfterScarePayload.ID, SpawnMobAfterScarePayload.CODEC);
    }
}
