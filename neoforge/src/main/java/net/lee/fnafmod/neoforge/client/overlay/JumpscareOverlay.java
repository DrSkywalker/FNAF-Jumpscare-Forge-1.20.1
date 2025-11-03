package net.lee.fnafmod.neoforge.client.overlay;

import net.lee.fnafmod.client.JumpscareManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.gui.layers.GuiLayer;

public class JumpscareOverlay implements GuiLayer {
    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        JumpscareManager.get().render(guiGraphics);
    }
}
