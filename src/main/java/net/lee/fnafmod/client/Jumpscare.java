package net.lee.fnafmod.client;

import net.minecraft.util.Identifier;

public record Jumpscare(String id, Identifier[] frames, int fps, Identifier soundKey, boolean loop,
                        String anchor, double scale, Identifier spawnMobId, String spawnName, int spawnOffX,
                        int spawnOffY, int spawnOffZ,
                        String[] armor) {

    public Jumpscare(
            String id,
            Identifier[] frames,
            int fps,
            Identifier soundKey,
            boolean loop,
            String anchor,
            double scale,
            Identifier spawnMobId,
            String spawnName,
            int spawnOffX, int spawnOffY, int spawnOffZ,
            String[] armor
    ) {
        this.id = id;
        this.frames = frames;
        this.fps = fps;
        this.soundKey = soundKey;
        this.loop = loop;
        this.anchor = (anchor == null || anchor.isEmpty()) ? "fullscreen" : anchor;
        this.scale = (scale <= 0) ? 1.0 : scale;
        this.spawnMobId = spawnMobId;
        this.spawnName = (spawnName == null || spawnName.isBlank()) ? null : spawnName;
        this.spawnOffX = spawnOffX;
        this.spawnOffY = spawnOffY;
        this.spawnOffZ = spawnOffZ;
        this.armor = (armor == null) ? null : armor;
    }

    public Jumpscare(
            String id,
            Identifier[] frames,
            int fps,
            Identifier soundKey
    ) {
        this(id, frames, fps, soundKey,
                false, "fullscreen", 1.0,
                null, null, 0, 0, 0,
                null);
    }

    public static Identifier id(String full) {
        return Identifier.tryParse(full);
    }
}
