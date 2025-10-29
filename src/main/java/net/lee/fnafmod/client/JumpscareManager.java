package net.lee.fnafmod.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvent;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JumpscareManager {

    private static final JumpscareManager INSTANCE = new JumpscareManager();
    public static JumpscareManager get() { return INSTANCE; }

    private long lastTriggerNs = 0L;
    private static final long MIN_RETRIGGER_NS = 250_000_000L; // 250 ms

    private final Random rng = new Random();
    private final List<Jumpscare> catalog = new ArrayList<>();
    private final List<Jumpscare> unusedPool = new ArrayList<>();

    // Active jumpscare state
    private Jumpscare active = null;
    private long startNanos = 0L;
    private boolean soundPlayed = false;

    // --- Random auto trigger scheduler ---
    private static final double RANDOM_MIN_SECONDS = 45.0;  // Min gap between random scares
    private static final double RANDOM_MAX_SECONDS = 180.0; // Max gap
    private long nextTriggerAtNanos = -1L;
    public boolean isActive() { return active != null; }
    // --- Hold last frame slightly ---
    private static final int HOLD_LAST_MS = 140;

    private JumpscareManager() {
        loadCatalog();
        if (nextTriggerAtNanos < 0L) {
            scheduleNext();
        }
    }

    private long nowNs() { return System.nanoTime(); }

    private long randomDelayNs(double minSec, double maxSec) {
        double span = Math.max(0.0, maxSec - minSec);
        double sec = minSec + rng.nextDouble() * span;
        return (long) (sec * 1_000_000_000L);
    }

    private void scheduleNext() {
        nextTriggerAtNanos = nowNs() + randomDelayNs(RANDOM_MIN_SECONDS, RANDOM_MAX_SECONDS);
    }

    private void loadCatalog() {
        try {
            ResourceLocation manifest = ResourceLocation.parse("fnafmod:jumpscares/jumpscares.json");
            Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(manifest);
            if (res.isEmpty()) return;
            JsonObject root = new Gson().fromJson(new InputStreamReader(res.get().open(), StandardCharsets.UTF_8), JsonObject.class);
            JsonArray arr = root.getAsJsonArray("entries");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject e = arr.get(i).getAsJsonObject();
                String id = e.get("id").getAsString();
                String folder = e.get("folder").getAsString();
                String pattern = e.get("pattern").getAsString();
                int frameCount = e.get("frameCount").getAsInt();
                int fps = e.get("fps").getAsInt();
                String sound = e.get("sound").getAsString();

                ResourceLocation[] frames = new ResourceLocation[frameCount];
                for (int f = 0; f < frameCount; f++) {
                    String file = String.format(pattern, f + 1);
                    frames[f] = ResourceLocation.parse(folder + file);
                }
                catalog.add(new Jumpscare(id, frames, fps, ResourceLocation.parse(sound)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void triggerRandom() {
        if (active != null) return;
        long now = System.nanoTime();
        if (now - lastTriggerNs < MIN_RETRIGGER_NS) return;

        if (unusedPool.isEmpty()) { unusedPool.addAll(catalog); Collections.shuffle(unusedPool, rng); }
        Jumpscare next = unusedPool.remove(0);
        trigger(next);
        lastTriggerNs = now;
    }

    public void trigger(Jumpscare js) {
        if (active != null) return;                 // ignore if already playing
        this.active = js;
        this.startNanos = System.nanoTime();

        // play once, right now (UI channel, no 3D attenuation)
        var mc = Minecraft.getInstance();
        SoundEvent se = SoundEvent.createVariableRangeEvent(js.soundKey);
        mc.getSoundManager().play(SimpleSoundInstance.forUI(se, 1.0f));
    }

    // ⏰ Tick called every frame (already registered in ClientEvents)
    public void tick() {
        if (active != null) return; // currently showing jumpscare

        long now = nowNs();
        if (nextTriggerAtNanos > 0L && now >= nextTriggerAtNanos && !catalog.isEmpty()) {
            triggerRandom();
            scheduleNext();
        }
    }

    private int currentFrameIndex(Jumpscare js) {
        long elapsedNs = System.nanoTime() - startNanos;
        double t = elapsedNs / 1_000_000_000.0;
        int idx = (int) Math.floor(t * js.fps);
        return Math.min(idx, js.frames.length - 1);
    }

    public void render(GuiGraphics g) {
        if (active == null) return;
        var mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        if (!soundPlayed) {
            SoundEvent se = SoundEvent.createVariableRangeEvent(active.soundKey);
            mc.getSoundManager().play(SimpleSoundInstance.forUI(se, 1.0f));
            soundPlayed = true;
        }

        int idx = currentFrameIndex(active);
        ResourceLocation frame = active.frames[idx];

        g.blit(frame, 0, 0, 0, 0, sw, sh, sw, sh);

        double duration = active.frames.length / (double) active.fps;
        duration += HOLD_LAST_MS / 1000.0;
        double elapsed = (System.nanoTime() - startNanos) / 1_000_000_000.0;

        if (elapsed > duration) {
            active = null;
            // Optional cooldown padding after a jumpscare
            if (nextTriggerAtNanos < nowNs() + (long) (RANDOM_MIN_SECONDS * 1_000_000_000L)) {
                scheduleNext();
            }
        }
    }
    // Manual reset if you ever need it
    public void resetCycle() {
        unusedPool.clear();
        scheduleNext();
    }
}
