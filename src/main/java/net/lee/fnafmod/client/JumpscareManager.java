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

    // --- Random check window (NOT guaranteed scare) ---
    private static final double CHECK_MIN_SECONDS = 60.0;   // earliest time to check
    private static final double CHECK_MAX_SECONDS = 120.0;  // latest time to check

    // --- Probabilities ---
    private static final double BASE_TRIGGER_CHANCE = 0.30; // 30% per check
    private static final double IDLE_MULTIPLIER     = 1.6;  // boost when a Screen is open
    private static final double MAX_CHANCE_CLAMP    = 0.85; // never exceed 85%

    // --- Cooldown (avoid back-to-back) ---
    private static final double MIN_COOLDOWN_SECONDS = 10.0;

    // --- Scheduler state (when to *check*, not guaranteed trigger) ---
    private long nextCheckAtNanos = -1L;

    private final Random rng = new Random();
    private final List<Jumpscare> catalog = new ArrayList<>();
    private final List<Jumpscare> unusedPool = new ArrayList<>();

    // Active jumpscare state
    private Jumpscare active = null;
    private long startNanos = 0L;
    private boolean soundPlayed = false;
    // Max gap
    private long nextTriggerAtNanos = -1L;
    public boolean isActive() { return active != null; }
    // --- Hold last frame slightly ---
    private static final int HOLD_LAST_MS = 140;

    private JumpscareManager() {
        loadCatalog();
        if (nextTriggerAtNanos < 0L) {
            scheduleNextCheckFromNow();
        }
    }

    private long nowNs() { return System.nanoTime(); }

    private long randomDelayNs(double minSec, double maxSec) {
        double span = Math.max(0.0, maxSec - minSec);
        double sec  = minSec + rng.nextDouble() * span;
        return (long) (sec * 1_000_000_000L);
    }

    private void scheduleNextCheckFromNow() {
        nextCheckAtNanos = nowNs() + randomDelayNs(CHECK_MIN_SECONDS, CHECK_MAX_SECONDS);
    }

    private boolean isIdleContext() {
        // “Idle” = any GUI is open (title, options, pause, inventory, etc.)
        return net.minecraft.client.Minecraft.getInstance().screen != null;
    }

    private double currentTriggerChance() {
        double chance = BASE_TRIGGER_CHANCE;
        if (isIdleContext()) chance *= IDLE_MULTIPLIER;
        return Math.min(chance, MAX_CHANCE_CLAMP);
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
        if (active != null) return;            // ignore if already playing
        this.active = js;
        this.startNanos = System.nanoTime();

        // Play once, right now (UI channel)
        var mc = net.minecraft.client.Minecraft.getInstance();
        net.minecraft.sounds.SoundEvent se =
                net.minecraft.sounds.SoundEvent.createVariableRangeEvent(js.soundKey);
        mc.getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(se, 1.0f)
        );
    }

    public void tick() {
        // Don’t schedule while a jumpscare is active
        if (active != null) return;

        long now = nowNs();
        // Time to CHECK?
        if (nextCheckAtNanos > 0L && now >= nextCheckAtNanos && !catalog.isEmpty()) {
            double p = currentTriggerChance();
            if (rng.nextDouble() < p) {
                // Success: trigger one and set a short cooldown before next check
                triggerRandom(); // your existing non-repeating pool method is fine
                nextCheckAtNanos = now + (long) (MIN_COOLDOWN_SECONDS * 1_000_000_000L);
            } else {
                // Missed the roll → schedule the next check window
                scheduleNextCheckFromNow();
            }
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

            // Ensure the next probability CHECK isn’t immediately after the scare ends
            long soonest = nowNs() + (long) (MIN_COOLDOWN_SECONDS * 1_000_000_000L);
            if (nextCheckAtNanos < soonest) nextCheckAtNanos = soonest;
        }

    }
    // Manual reset if you ever need it
    public void resetCycle() {
        unusedPool.clear();
        scheduleNextCheckFromNow();
    }
}
