package net.lee.fnafmod.client;

import net.minecraft.resources.ResourceLocation;

/**
 * @param id         === Core required fields === e.g. "fnafmod:deedee"
 * @param frames     PNG frames for animation
 * @param fps        frames per second
 * @param soundKey   sound event resource
 * @param loop       === Optional metadata (for special behavior) === new — whether to loop until sound ends
 * @param anchor     "fullscreen", "bottom_left", etc.
 * @param scale      fraction of screen height
 * @param spawnMobId e.g. "minecraft:zombie" (null if none)
 */
public record Jumpscare(String id, ResourceLocation[] frames, int fps, ResourceLocation soundKey, boolean loop,
                        String anchor, double scale, String spawnMobId, int spawnOffX, int spawnOffY, int spawnOffZ) {

    /**
     * Primary constructor — full metadata.
     */
    public Jumpscare(
            String id,
            ResourceLocation[] frames,
            int fps,
            ResourceLocation soundKey,
            boolean loop,
            String anchor,
            double scale,
            String spawnMobId,
            int spawnOffX, int spawnOffY, int spawnOffZ
    ) {
        this.id = id;
        this.frames = frames;
        this.fps = fps;
        this.soundKey = soundKey;

        // defaults
        this.loop = loop;
        this.anchor = (anchor == null || anchor.isEmpty()) ? "fullscreen" : anchor;
        this.scale = (scale <= 0) ? 1.0 : scale;
        this.spawnMobId = (spawnMobId == null || spawnMobId.isBlank()) ? null : spawnMobId;
        this.spawnOffX = spawnOffX;
        this.spawnOffY = spawnOffY;
        this.spawnOffZ = spawnOffZ;
    }

    /**
     * Convenience constructor — for legacy entries without loop/metadata.
     */
    public Jumpscare(
            String id,
            ResourceLocation[] frames,
            int fps,
            ResourceLocation soundKey
    ) {
        this(id, frames, fps, soundKey,
                false, "fullscreen", 1.0,
                null, 0, 0, 0);
    }

    /**
     * Safe helper for creating ResourceLocations (no deprecation warnings).
     */
    public static ResourceLocation rl(String full) {
        return ResourceLocation.tryParse(full);
    }

}
