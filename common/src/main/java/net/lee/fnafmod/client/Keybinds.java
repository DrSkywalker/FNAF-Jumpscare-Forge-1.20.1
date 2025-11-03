package net.lee.fnafmod.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class Keybinds {

    public static KeyMapping TEST_SCARE;

    public static void init() {
        TEST_SCARE = new KeyMapping(
                "key.fnafmod.test_scare",
                GLFW.GLFW_KEY_F6,
                "key.categories.fnafmod");
    }
}

