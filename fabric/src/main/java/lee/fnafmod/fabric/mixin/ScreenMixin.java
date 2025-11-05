package lee.fnafmod.fabric.mixin;

import lee.fnafmod.client.JumpscareManager;
import lee.fnafmod.client.Keybinds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {

    /**
     * Inject into the render method to render jumpscares on top of screens
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        JumpscareManager.get().render(guiGraphics);
    }

    /**
     * Inject into the keyPressed method to handle debug keybinds (F6/F7) in screens
     */
    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void onKeyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (!JumpscareManager.get().isActive()) {
            if (Keybinds.TEST_SCARE != null && keyEvent.input() == GLFW.GLFW_KEY_F6) {
                JumpscareManager.get().triggerRandom();
            }
            if (Keybinds.TEST_FIRST_SCARE != null && keyEvent.input() == GLFW.GLFW_KEY_F7) {
                JumpscareManager.get().triggerFirst();
            }
        }
    }
}