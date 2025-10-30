// java
package net.lee.fnafmod.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import net.lee.fnafmod.network.FnafNet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvent;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JumpscareManager {

    private static final JumpscareManager INSTANCE = new JumpscareManager();
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
    private static final java.util.Map<ResourceLocation, int[]> TEX_SIZE_CACHE = new ConcurrentHashMap<>();
    private static final java.util.Map<ResourceLocation, SoundEvent> SOUND_CACHE = new ConcurrentHashMap<>();

    private final Random rng = new Random();
    private final List<Jumpscare> catalog = new ArrayList<>();
    private final List<Jumpscare> unusedPool = new ArrayList<>();
    private long lastTriggerNs = 0L;
    private long nextCheckAtNanos = -1L;

    private Jumpscare active = null;
    private long startNanos = 0L;
    private SimpleSoundInstance playingSound = null;

    private JumpscareManager() {
        loadCatalog();
        scheduleNextCheckFromNow();
    }

    private static int[] getTextureSize(ResourceLocation id) {
        int[] cached = TEX_SIZE_CACHE.get(id);
        if (cached != null) return cached;

        try {
            var rm = Minecraft.getInstance().getResourceManager();
            Optional<Resource> opt = rm.getResource(id);
            if (opt.isPresent()) {
                try (var in = opt.get().open()) {
                    com.mojang.blaze3d.platform.NativeImage img = com.mojang.blaze3d.platform.NativeImage.read(in);
                    int[] dims = new int[]{img.getWidth(), img.getHeight()};
                    img.close();
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

    private boolean isIdleContext(Minecraft mc) {
        return mc.screen != null;
    }

    private double currentTriggerChance(Minecraft mc) {
        double chance = BASE_TRIGGER_CHANCE;
        if (isIdleContext(mc)) chance *= IDLE_MULTIPLIER;
        return Math.min(chance, MAX_CHANCE_CLAMP);
    }

    private void loadCatalog() {
        try {
            ResourceLocation manifest = ResourceLocation.parse("fnafmod:jumpscares/jumpscares.json");
            Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(manifest);
            if (res.isEmpty()) return;
            JsonObject root = new Gson().fromJson(
                    new InputStreamReader(res.get().open(), StandardCharsets.UTF_8),
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
                String spawnMobId = e.has("spawn_mob") ? e.get("spawn_mob").getAsString() : null;
                int offX = 0, offY = 0, offZ = 0;
                if (e.has("spawn_offset")) {
                    JsonArray off = e.getAsJsonArray("spawn_offset");
                    if (off.size() >= 3) {
                        offX = off.get(0).getAsInt();
                        offY = off.get(1).getAsInt();
                        offZ = off.get(2).getAsInt();
                    }
                }
                String folderPrefix = folder.endsWith("/") ? folder : (folder + "/");
                ResourceLocation[] frames = new ResourceLocation[frameCount];
                for (int f = 0; f < frameCount; f++) {
                    String file = String.format(pattern, f + 1);
                    frames[f] = ResourceLocation.parse(folderPrefix + file);
                }
                catalog.add(new Jumpscare(
                        id,
                        frames,
                        fps,
                        ResourceLocation.parse(sound),
                        loop,
                        anchor,
                        scale,
                        spawnMobId,
                        offX, offY, offZ
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
            unusedPool.addAll(catalog);
            Collections.shuffle(unusedPool, rng);
        }
        Jumpscare next = unusedPool.remove(0);
        trigger(next);
        lastTriggerNs = now;
    }

    public void trigger(Jumpscare js) {
        if (active != null) return;
        this.active = js;
        this.startNanos = nowNs();

        Minecraft mc = Minecraft.getInstance();
        SoundEvent se = SOUND_CACHE.computeIfAbsent(js.soundKey(), SoundEvent::createVariableRangeEvent);
        playingSound = SimpleSoundInstance.forUI(se, 1.0f);
        mc.getSoundManager().play(playingSound);
    }

    public void tick() {
        if (active != null) return;

        long now = nowNs();
        Minecraft mc = Minecraft.getInstance();
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

    public void render(GuiGraphics g) {
        if (active == null) return;
        Minecraft mc = Minecraft.getInstance();

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

        ResourceLocation frame = active.frames()[idx];

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // Push a high Z translation and disable depth testing so the jumpscare draws on top
        g.pose().pushPose();
        g.pose().translate(0.0F, 0.0F, 1000.0F);
        RenderSystem.disableDepthTest();

        if ("bottom_left".equalsIgnoreCase(active.anchor())) {
            int[] dims = getTextureSize(frame);
            int texW = dims[0];
            int texH = dims[1];

            int drawH = (int) Math.max(16, sh * active.scale());
            int drawW = Math.max(16, (int) Math.round(drawH * (texW / (double) texH)));

            int pad = 6;
            int x = pad;
            int y = sh - drawH - pad;

            // draw with computed position and size
            g.blit(frame, x, y, drawW, drawH, 0, 0, texW, texH, texW, texH);
        } else {
            g.blit(frame, 0, 0, 0, 0,
                    sw,
                    sh,
                    sw,
                    sh);
        }

        boolean finish;
        if (active.loop()) {
            boolean soundActive = (playingSound != null) && mc.getSoundManager().isActive(playingSound);
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
                if (mc.player != null && mc.level != null && mc.screen == null) {
                    FnafNet.CHANNEL.sendToServer(
                            new net.lee.fnafmod.network.SpawnMobAfterScareC2S(
                                    finished.spawnMobId(),
                                    finished.spawnOffX(),
                                    finished.spawnOffY(),
                                    finished.spawnOffZ()
                            )
                    );
                }
            }
        }
    }
}