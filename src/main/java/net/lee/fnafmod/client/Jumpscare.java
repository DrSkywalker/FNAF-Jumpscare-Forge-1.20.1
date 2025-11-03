package net.lee.fnafmod.client;

import net.minecraft.resources.ResourceLocation;

public record Jumpscare(String id, ResourceLocation[] frames, int fps, ResourceLocation soundKey, boolean loop,
                        String anchor, double scale, ResourceLocation spawnMobId, String spawnName, int spawnOffX,
                        int spawnOffY, int spawnOffZ,
                        String[] armor, AnchorType anchorType, boolean isXor) {

    public enum AnchorType {
        FULLSCREEN,
        BOTTOM_LEFT,
        XOR
    }

    // Helper method to normalize anchor value
    private static String normalizeAnchor(String anchor) {
        return (anchor == null || anchor.isEmpty()) ? "fullscreen" : anchor;
    }

    // Helper method to compute anchor type
    private static AnchorType computeAnchorType(String id, String anchor) {
        String normalizedAnchor = normalizeAnchor(anchor);
        if (computeIsXor(id)) {
            return AnchorType.XOR;
        } else if ("bottom_left".equalsIgnoreCase(normalizedAnchor)) {
            return AnchorType.BOTTOM_LEFT;
        } else {
            return AnchorType.FULLSCREEN;
        }
    }

    // Helper method to check if ID is XOR
    private static boolean computeIsXor(String id) {
        return "fnafmod:xor".equalsIgnoreCase(id);
    }

    public Jumpscare(
            String id,
            ResourceLocation[] frames,
            int fps,
            ResourceLocation soundKey,
            boolean loop,
            String anchor,
            double scale,
            ResourceLocation spawnMobId,
            String spawnName,
            int spawnOffX, int spawnOffY, int spawnOffZ,
            String[] armor
    ) {
        this(
                id,
                frames,
                fps,
                soundKey,
                loop,
                normalizeAnchor(anchor),
                (scale <= 0) ? 1.0 : scale,
                spawnMobId,
                (spawnName == null || spawnName.isBlank()) ? null : spawnName,
                spawnOffX,
                spawnOffY,
                spawnOffZ,
                (armor == null) ? null : armor,
                computeAnchorType(id, anchor),
                computeIsXor(id)
        );
    }

    public Jumpscare(
            String id,
            ResourceLocation[] frames,
            int fps,
            ResourceLocation soundKey
    ) {
        this(id, frames, fps, soundKey,
                false, "fullscreen", 1.0,
                null, null, 0, 0, 0,
                null);
    }

    public static ResourceLocation rl(String full) {
        return ResourceLocation.tryParse(full);
    }
}
