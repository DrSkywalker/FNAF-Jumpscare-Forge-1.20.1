package lee.fnafmod.fabric.client;

import lee.fnafmod.client.JumpscareManager;
import lee.fnafmod.client.Keybinds;
import lee.fnafmod.platform.ClientEventHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class FabricClientEventHandler implements ClientEventHandler {

    @Override
    public void registerClientEvents() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (Keybinds.TEST_SCARE != null && Keybinds.TEST_SCARE.consumeClick()) {
                JumpscareManager.get().triggerRandom();
            }
            if (Keybinds.TEST_FIRST_SCARE != null && Keybinds.TEST_FIRST_SCARE.consumeClick()) {
                JumpscareManager.get().triggerFirst();
            }
            JumpscareManager.get().tick();
        });
    }

    @Override
    public void registerKeyBindings() {
        if (Keybinds.TEST_SCARE != null) {
            KeyBindingHelper.registerKeyBinding(Keybinds.TEST_SCARE);
        }
        if (Keybinds.TEST_FIRST_SCARE != null) {
            KeyBindingHelper.registerKeyBinding(Keybinds.TEST_FIRST_SCARE);
        }
    }

    @Override
    public void registerRenderOverlay() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> JumpscareManager.get().render(drawContext));
    }
}