package net.lee.fnafmod.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Keybinds {

    public static KeyBinding TEST_SCARE;

    public static void register() {
        TEST_SCARE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fnafmod.test_scare",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                "key.categories.fnafmod"));
    }
}

