package net.lee.fnafmod.client.overlay;

import net.lee.fnafmod.client.JumpscareManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class JumpscareOverlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        JumpscareManager.get().render(graphics);
    }
}
