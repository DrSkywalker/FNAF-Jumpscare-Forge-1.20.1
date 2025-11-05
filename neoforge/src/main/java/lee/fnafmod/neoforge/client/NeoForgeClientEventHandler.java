package lee.fnafmod.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import lee.fnafmod.client.JumpscareManager;
import lee.fnafmod.client.Keybinds;
import lee.fnafmod.fnafmod;
import lee.fnafmod.neoforge.client.overlay.JumpscareOverlay;
import lee.fnafmod.platform.ClientEventHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public class NeoForgeClientEventHandler implements ClientEventHandler {

    private final IEventBus modBus;

    public NeoForgeClientEventHandler(IEventBus modBus) {
        this.modBus = modBus;
    }

    @Override
    public void registerClientEvents() {
        NeoForge.EVENT_BUS.addListener(this::onClientTickEnd);
        NeoForge.EVENT_BUS.addListener(this::onKeyInput);
        NeoForge.EVENT_BUS.addListener(this::onScreenRenderPost);
    }

    @Override
    public void registerKeyBindings() {
        modBus.addListener(this::onRegisterKeyMappings);
    }

    @Override
    public void registerRenderOverlay() {
        modBus.addListener(this::onRegisterGuiOverlays);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent e) {
        if (Keybinds.TEST_SCARE != null) {
            e.register(Keybinds.TEST_SCARE);
        }
        if (Keybinds.TEST_FIRST_SCARE != null) {
            e.register(Keybinds.TEST_FIRST_SCARE);
        }
    }

    private void onRegisterGuiOverlays(RegisterGuiLayersEvent e) {
        e.registerAboveAll(ResourceLocation.parse(fnafmod.MOD_ID), new JumpscareOverlay());
    }

    private void onClientTickEnd(ClientTickEvent.Post e) {
        if (Keybinds.TEST_SCARE != null && Keybinds.TEST_SCARE.consumeClick()) {
            JumpscareManager.get().triggerRandom();
        }
        if (Keybinds.TEST_FIRST_SCARE != null && Keybinds.TEST_FIRST_SCARE.consumeClick()) {
            JumpscareManager.get().triggerFirst();
        }
        JumpscareManager.get().tick();
    }

    private void onKeyInput(InputEvent.Key e) {
        if (e.getAction() == GLFW.GLFW_PRESS && !JumpscareManager.get().isActive()) {
            InputConstants.Key pressedKey = (e.getKey() == 0 && e.getScanCode() > 0)
                    ? InputConstants.Type.SCANCODE.getOrCreate(e.getScanCode())
                    : InputConstants.Type.KEYSYM.getOrCreate(e.getKey());

            if (Keybinds.TEST_SCARE != null && Keybinds.TEST_SCARE.matches(e.getKeyEvent())) {
                JumpscareManager.get().triggerRandom();
            }
            if (Keybinds.TEST_FIRST_SCARE != null && Keybinds.TEST_FIRST_SCARE.matches(e.getKeyEvent())) {
                JumpscareManager.get().triggerFirst();
            }
        }
    }

    private void onScreenRenderPost(ScreenEvent.Render.Post e) {
        JumpscareManager.get().render(e.getGuiGraphics());
    }
}