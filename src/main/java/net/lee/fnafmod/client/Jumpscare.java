package net.lee.fnafmod.client;

import net.minecraft.resources.ResourceLocation;

/**
 * Simple immutable data class for one jumpscare animation.
 * Frames and sound are defined in assets/fnafmod/jumpscares/jumpscares.json
 */
public class Jumpscare {

    // Fields made public so JumpscareManager can read them directly
    public final String id;                  // e.g., fnafmod:withered_foxy
    public final ResourceLocation[] frames;  // All texture frame paths
    public final int fps;                    // Frames per second
    public final ResourceLocation soundKey;  // SoundEvent id (e.g., fnafmod:jumpscares.foxy)

    public Jumpscare(String id, ResourceLocation[] frames, int fps, ResourceLocation soundKey) {
        this.id = id;
        this.frames = frames;
        this.fps = fps;
        this.soundKey = soundKey;
    }
}
