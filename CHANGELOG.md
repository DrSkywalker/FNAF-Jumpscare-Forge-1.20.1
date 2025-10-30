# 🐻 FNAF Jumpscare Mod — Changelog

All notable changes to this project will be documented here.  
This project follows [Semantic Versioning](https://semver.org/).

---
## 1.0.3 – 2025-10-30

> _Server Update_

### ⚙️ Technical
- Added Check to prevent Forge Server crashes on Loading the Mod.
- Forge Servers now fully compatible with the Mod.

## 1.0.2 – 2025-10-30

> _DeeDee Update_

### ✨ Features

- Adds DeeDee as a jumpscare character

### 🔊 Audio

- Adds DeeDee sound effects

### 🎨 Visuals

- Adds DeeDee animated PNG frames

### ⚙️ Technical

- Makes DeeDee's Jumpscare only in the Bottom Left corner of the screen
- Gives DeeDee a lower probability to appear compared to other characters
- Gives DeeDee The ability to spawn hostile mobs (Creepers, Skeletons, Zombies) when she appears
- Updated sounds.json and characters.json to include DeeDee

## 1.0.1 – 2025-10-30

> _Probabilities Update_

### ✨ Features

- Added Probabilities to Active Jumpscares
- Tweaked jumpscare probability for better pacing
- Fixed overlapping sound playback issue
- Improved idle detection during menus
- Added Readme with mod details and credits

## 1.0.0 – 2025-10-29

> _Initial Public Release_

### ✨ Features

- Added **UCN-style jumpscare system** supporting animated PNG frames and OGG sounds.
- Works on **any Minecraft screen** — title, menus, inventory, and in-game.
- Supports **multiple characters** (FNAF 2 cast).
- **Non-repeating randomizer**: each character appears once before repeating.
- **Lag-proof scheduler**: triggers automatically every 45–180 seconds without FPS issues.
- **Manual F6 trigger** for testing jumpscares.
- Added **custom sound support** via `sounds.json`.
- **Localization** for keybinds (English ready).

### 🎨 Visuals

- Added **logo.png** for Forge Mods list.

### 🔊 Audio

- Unified all jumpscares to use Foxy’s SFX for consistency.
- Improved playback timing to prevent sound duplication or echo.

### ⚙️ Technical

- Built for **Forge 47.4.10 (Minecraft 1.20.1)**.
- Requires **Java 17**.
- Uses Mojang official mappings.
- Assets are all lowercase for resource safety.
- Random scheduler and frame timing use real-time (`System.nanoTime`) for stability.

---

## **Future Plans**

- Add toggle/config for enabling/disabling random scares.
- Add optional fade-in/out and light flash effects.
- Consider Fabric port.
- Expand character roster to include more FNAF animatronics.

---

_© 2025 Lee — FNAF Jumpscare Mod. This mod is a fan project inspired by Five Nights at Freddy’s._
