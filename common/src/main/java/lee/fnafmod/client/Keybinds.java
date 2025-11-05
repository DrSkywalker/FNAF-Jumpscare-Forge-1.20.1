package lee.fnafmod.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class Keybinds {

    public static KeyMapping TEST_SCARE;
    public static KeyMapping TEST_FIRST_SCARE;
    public static KeyMapping.Category FNAF_MOD;

    public static void init() {
        TEST_SCARE = new KeyMapping(
                "key.fnafmod.test_scare",
                GLFW.GLFW_KEY_F6,
                FNAF_MOD);
        TEST_FIRST_SCARE = new KeyMapping(
                "key.fnafmod.test_first_scare",
                GLFW.GLFW_KEY_F7,
                FNAF_MOD);
    }
}