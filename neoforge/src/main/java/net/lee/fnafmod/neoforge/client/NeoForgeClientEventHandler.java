package net.lee.fnafmod.neoforge.client;

import net.lee.fnafmod.FnafMod;
import net.lee.fnafmod.client.JumpscareManager;
import net.lee.fnafmod.client.Keybinds;
import net.lee.fnafmod.neoforge.client.overlay.JumpscareOverlay;
import net.lee.fnafmod.platform.ClientEventHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

public class NeoForgeClientEventHandler implements ClientEventHandler {
    
    private final IEventBus modBus;
    
    public NeoForgeClientEventHandler(IEventBus modBus) {
        this.modBus = modBus;
    }
    
    @Override
    public void registerClientEvents() {
        // Register tick event
        NeoForge.EVENT_BUS.addListener(this::onClientTickEnd);
        // Register key input event
        NeoForge.EVENT_BUS.addListener(this::onKeyInput);
        // Register screen render event
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
    }
    
    private void onRegisterGuiOverlays(RegisterGuiLayersEvent e) {
        // Works in-world when no Screen is open
        e.registerAboveAll(FnafMod.MOD_ID, new JumpscareOverlay());
    }
    
    private void onClientTickEnd(ClientTickEvent.Post e) {
        if (Keybinds.TEST_SCARE != null && Keybinds.TEST_SCARE.consumeClick()) {
            JumpscareManager.get().triggerRandom();
        }
        JumpscareManager.get().tick();
    }
    
    private void onKeyInput(InputEvent.Key e) {
        if (e.getAction() == GLFW.GLFW_PRESS
                && Keybinds.TEST_SCARE != null
                && Keybinds.TEST_SCARE.matches(e.getKey(), e.getScanCode())
                && !JumpscareManager.get().isActive()) {
            JumpscareManager.get().triggerRandom();
        }
    }
    
    private void onScreenRenderPost(ScreenEvent.Render.Post e) {
        JumpscareManager.get().render(e.getGuiGraphics());
    }
}
