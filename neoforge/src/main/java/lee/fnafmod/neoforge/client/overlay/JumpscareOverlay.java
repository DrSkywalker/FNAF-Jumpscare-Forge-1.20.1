package lee.fnafmod.neoforge.client.overlay;

import lee.fnafmod.client.JumpscareManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.GuiLayer;

public class JumpscareOverlay implements GuiLayer {
    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        JumpscareManager.get().render(guiGraphics);
    }
}
