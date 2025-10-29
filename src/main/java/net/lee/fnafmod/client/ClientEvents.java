package net.lee.fnafmod.client;

import net.lee.fnafmod.client.overlay.JumpscareOverlay;
import net.lee.fnafmod.fnafmod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = fnafmod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    public static void init() {
        // Non-MOD bus events
        MinecraftForge.EVENT_BUS.addListener(ClientEvents::onClientTickEnd);
        MinecraftForge.EVENT_BUS.addListener(ClientEvents::onScreenRenderPost);
        MinecraftForge.EVENT_BUS.addListener(ClientEvents::onKeyInput); // <-- add this line
    }

    // ========== MOD BUS EVENTS ==========
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent e) {
        Keybinds.register(e);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent e) {
        // Works in-world when no Screen is open
        e.registerAboveAll("jumpscare", new JumpscareOverlay());
    }

    // ========== FORGE BUS EVENTS ==========
    // Trigger via tick (in-world)
    public static void onClientTickEnd(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            if (Keybinds.TEST_SCARE.consumeClick()) {
                JumpscareManager.get().triggerRandom();
            }
            JumpscareManager.get().tick();
        }
    }

    public static void onKeyInput(InputEvent.Key e) {
        if (e.getAction() == GLFW.GLFW_PRESS
                && Keybinds.TEST_SCARE.matches(e.getKey(), e.getScanCode())
                && !JumpscareManager.get().isActive()) {
            JumpscareManager.get().triggerRandom();
        }
    }

    // Render jumpscare on top of any GUI screen (Title, Inventory, etc.)
    public static void onScreenRenderPost(ScreenEvent.Render.Post e) {
        JumpscareManager.get().render(e.getGuiGraphics());
    }
}