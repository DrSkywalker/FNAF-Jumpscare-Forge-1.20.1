package net.lee.fnafmod.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class Keybinds {

    public static KeyMapping TEST_SCARE;

    public static void register(RegisterKeyMappingsEvent e) {
        TEST_SCARE = new KeyMapping(
                "key.fnafmod.test_scare",
                GLFW.GLFW_KEY_F6,
                "key.categories.fnafmod");
        e.register(TEST_SCARE);
    }
}

