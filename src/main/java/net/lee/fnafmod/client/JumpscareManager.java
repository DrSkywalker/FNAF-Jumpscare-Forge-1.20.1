package net.lee.fnafmod.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lee.fnafmod.network.SpawnMobAfterScarePayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.resource.Resource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JumpscareManager {

    private static final long MIN_RETRIGGER_NS = 250_000_000L; // 250 ms
    private static final double CHECK_MIN_SECONDS = 60.0;
    private static final double CHECK_MAX_SECONDS = 120.0;
    private static final double BASE_TRIGGER_CHANCE = 0.30;
    private static final double IDLE_MULTIPLIER = 1.6;
    private static final double MAX_CHANCE_CLAMP = 0.85;
    private static final double MIN_COOLDOWN_SECONDS = 10.0;
    private static final int HOLD_LAST_SECS = 0;
    private static final double MAX_FALLBACK_AUDIO_SECS = 12.0;
    private static final Gson GSON = new Gson();
    private static final Map<Identifier, int[]> TEX_SIZE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Identifier, SoundEvent> SOUND_CACHE = new ConcurrentHashMap<>();
    private static final JumpscareManager INSTANCE = new JumpscareManager();
    private final java.util.Random rng = new java.util.Random();
    private final List<Jumpscare> catalog = new ArrayList<>();
    private final List<Jumpscare> unusedPool = new ArrayList<>();
    private long lastTriggerNs = 0L;
    private long nextCheckAtNanos = -1L;
    private Jumpscare active = null;
    private long startNanos = 0L;
    private SoundInstance playingSound = null;

    private JumpscareManager() {
        loadCatalog();
        scheduleNextCheckFromNow();
    }

    private static int[] getTextureSize(Identifier id) {
        int[] cached = TEX_SIZE_CACHE.get(id);
        if (cached != null) return cached;

        try {
            var rm = MinecraftClient.getInstance().getResourceManager();
            Optional<Resource> opt = rm.getResource(id);
            if (opt.isPresent()) {
                try (var in = opt.get().getInputStream();
                     com.mojang.blaze3d.platform.NativeImage img = com.mojang.blaze3d.platform.NativeImage.read(in)) {
                    int[] dims = new int[]{img.getWidth(), img.getHeight()};
                    TEX_SIZE_CACHE.put(id, dims);
                    return dims;
                }
            }
        } catch (Exception ignored) {
        }

        return new int[]{256, 256};
    }

    public static JumpscareManager get() {
        return INSTANCE;
    }

    public boolean isActive() {
        return active != null;
    }

    private long nowNs() {
        return System.nanoTime();
    }

    private long randomDelayNs(double minSec, double maxSec) {
        double span = Math.max(0.0, maxSec - minSec);
        double sec = minSec + rng.nextDouble() * span;
        return (long) (sec * 1_000_000_000L);
    }

    private void scheduleNextCheckFromNow() {
        nextCheckAtNanos = nowNs() + randomDelayNs(CHECK_MIN_SECONDS, CHECK_MAX_SECONDS);
    }

    private boolean isIdleContext(MinecraftClient mc) {
        return mc.currentScreen != null;
    }

    private double currentTriggerChance(MinecraftClient mc) {
        double chance = BASE_TRIGGER_CHANCE;
        if (isIdleContext(mc)) chance *= IDLE_MULTIPLIER;
        return Math.min(chance, MAX_CHANCE_CLAMP);
    }

    private void loadCatalog() {
        try {
            Identifier manifest = Identifier.tryParse("fnafmod:jumpscares/jumpscares.json");
            if (manifest == null) return;
            Optional<Resource> res = MinecraftClient.getInstance().getResourceManager().getResource(manifest);
            if (res.isEmpty()) return;
            JsonObject root = GSON.fromJson(
                    new InputStreamReader(res.get().getInputStream(), StandardCharsets.UTF_8),
                    JsonObject.class
            );
            JsonArray arr = root.getAsJsonArray("entries");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject e = arr.get(i).getAsJsonObject();
                String id = e.get("id").getAsString();
                String folder = e.get("folder").getAsString();
                String pattern = e.get("pattern").getAsString();
                int frameCount = e.get("frameCount").getAsInt();
                int fps = e.get("fps").getAsInt();
                String sound = e.get("sound").getAsString();
                String anchor = e.has("anchor") ? e.get("anchor").getAsString() : "fullscreen";
                double scale = e.has("scale") ? e.get("scale").getAsDouble() : 1.0;
                boolean loop = e.has("loop") && e.get("loop").getAsBoolean();

                // determine whether this entry wants a mob spawn
                boolean wantsSpawn = e.has("spawn_mob") || (e.has("spawn") && e.get("spawn").getAsBoolean());

                Identifier spawnMobId = null;
                String spawnName = null;
                int offX = 0, offY = 0, offZ = 0;
                String[] armor = null;

                if (wantsSpawn && e.has("spawn_mob")) {
                    spawnMobId = Identifier.tryParse(e.get("spawn_mob").getAsString());
                }

                if (wantsSpawn && e.has("spawn_name")) {
                    spawnName = e.get("spawn_name").getAsString();
                    if (spawnName != null && spawnName.isBlank()) spawnName = null;
                }

                if (wantsSpawn && e.has("spawn_offset")) {
                    JsonArray off = e.getAsJsonArray("spawn_offset");
                    if (off.size() >= 3) {
                        offX = off.get(0).getAsInt();
                        offY = off.get(1).getAsInt();
                        offZ = off.get(2).getAsInt();
                    }
                }

                if (wantsSpawn && e.has("armor")) {
                    JsonArray armorArray = e.getAsJsonArray("armor");
                    armor = new String[armorArray.size()];
                    for (int j = 0; j < armorArray.size(); j++) {
                        armor[j] = armorArray.get(j).getAsString();
                    }
                }

                String folderPrefix = folder.endsWith("/") ? folder : (folder + "/");
                Identifier[] frames = new Identifier[frameCount];
                for (int f = 0; f < frameCount; f++) {
                    String file = String.format(pattern, f + 1);
                    frames[f] = Identifier.tryParse(folderPrefix + file);
                }

                Identifier soundLoc = Identifier.tryParse(sound);

                // construct with spawnName in the correct position
                catalog.add(new Jumpscare(
                        id,
                        frames,
                        fps,
                        soundLoc,
                        loop,
                        anchor,
                        scale,
                        spawnMobId,
                        spawnName,
                        offX, offY, offZ,
                        armor
                ));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void triggerRandom() {
        if (active != null) return;
        long now = nowNs();
        if (now - lastTriggerNs < MIN_RETRIGGER_NS) return;

        if (unusedPool.isEmpty()) {
            if (!catalog.isEmpty()) {
                unusedPool.addAll(catalog);
                Collections.shuffle(unusedPool, rng);
            }
        }

        if (unusedPool.isEmpty()) return;

        Jumpscare next = unusedPool.remove(0);
        trigger(next);
        lastTriggerNs = now;
    }

    public void trigger(Jumpscare js) {
        if (active != null) return;
        this.active = js;
        this.startNanos = nowNs();

        MinecraftClient mc = MinecraftClient.getInstance();
        SoundEvent se = SOUND_CACHE.computeIfAbsent(js.soundKey(), id -> SoundEvent.of(id));
        playingSound = PositionedSoundInstance.master(se, 1.0f);
        mc.getSoundManager().play(playingSound);
    }

    public void tick() {
        if (active != null) return;

        long now = nowNs();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (nextCheckAtNanos > 0L && now >= nextCheckAtNanos && !catalog.isEmpty()) {
            double p = currentTriggerChance(mc);
            if (rng.nextDouble() < p) {
                triggerRandom();
                nextCheckAtNanos = now + (long) (MIN_COOLDOWN_SECONDS * 1_000_000_000L);
            } else {
                scheduleNextCheckFromNow();
            }
        }
    }

    public void render(DrawContext g) {
        if (active == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();

        final double elapsed = (nowNs() - startNanos) / 1_000_000_000.0;
        final double frameDur = 1.0 / Math.max(1, active.fps());

        int idx;
        if (active.loop()) {
            idx = (int) Math.floor(elapsed / frameDur) % active.frames().length;
        } else {
            double totalAnim = active.frames().length * frameDur + HOLD_LAST_SECS;
            double t = Math.min(elapsed, totalAnim);
            idx = (int) Math.min(active.frames().length - 1, Math.floor(t / frameDur));
        }

        Identifier frame = active.frames()[idx];

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        // Push a high Z translation and disable depth testing so the jumpscare draws on top
        g.getMatrices().push();
        g.getMatrices().translate(0.0F, 0.0F, 1000.0F);
        RenderSystem.disableDepthTest();

        // Special-case: XOR should glitch rapidly side-to-side across the bottom of the screen
        if ("fnafmod:xor".equalsIgnoreCase(active.id())) {
            int[] dims = getTextureSize(frame);
            int texW = dims[0];
            int texH = dims[1];

            int drawH = (int) Math.max(16, sh * active.scale());
            int drawW = Math.max(16, (int) Math.round(drawH * (texW / (double) texH)));

            // Fast oscillation frequency (Hz). Increase for more rapid movement.
            double freqHz = 8.0;
            double t = elapsed;

            // Smooth ping-pong across the available horizontal range using sine
            double factor = (Math.sin(t * Math.PI * 2.0 * freqHz) + 1.0) * 0.5;
            int x = (int) Math.round(factor * Math.max(0, sw - drawW));

            // keep at bottom with small padding
            int pad = 6;
            int y = sh - drawH - pad;

            g.drawTexture(frame, x, y, 0, 0, drawW, drawH, texW, texH);
        } else if ("bottom_left".equalsIgnoreCase(active.anchor())) {
            int[] dims = getTextureSize(frame);
            int texW = dims[0];
            int texH = dims[1];

            int drawH = (int) Math.max(16, sh * active.scale());
            int drawW = Math.max(16, (int) Math.round(drawH * (texW / (double) texH)));

            int pad = 6;
            int x = pad;
            int y = sh - drawH - pad;

            // draw with computed position and size
            g.drawTexture(frame, x, y, 0, 0, drawW, drawH, texW, texH);
        } else {
            g.drawTexture(frame, 0, 0, 0, 0,
                    sw,
                    sh,
                    sw,
                    sh);
        }

        boolean finish;
        if (active.loop()) {
            boolean soundActive = (playingSound != null) && mc.getSoundManager().isPlaying(playingSound);
            boolean overFallback = elapsed > MAX_FALLBACK_AUDIO_SECS;
            finish = !soundActive || overFallback;
        } else {
            double endAt = active.frames().length * frameDur + HOLD_LAST_SECS;
            finish = elapsed > endAt;
        }

        if (finish) {
            Jumpscare finished = active;
            active = null;
            playingSound = null;

            long soonest = nowNs() + (long) (MIN_COOLDOWN_SECONDS * 1_000_000_000L);
            if (nextCheckAtNanos < soonest) nextCheckAtNanos = soonest;

            if (finished != null && finished.spawnMobId() != null) {
                if (mc.player != null && mc.world != null && mc.currentScreen == null) {
                    ClientPlayNetworking.send(
                            new SpawnMobAfterScarePayload(
                                    finished.spawnMobId(),
                                    finished.spawnName(),
                                    finished.spawnOffX(),
                                    finished.spawnOffY(),
                                    finished.spawnOffZ(),
                                    finished.armor()
                            )
                    );
                }
            }
        }

        RenderSystem.enableDepthTest();
        g.getMatrices().pop();
    }
}