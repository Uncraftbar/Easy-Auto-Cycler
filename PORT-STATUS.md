# NeoForge 26.3 — ported, client-tested, not published

Local-only preparation. No push, tag, release workflow or publisher task was run.
Version `3.1.2-local.26.3` is a local candidate identifier, not an approved release.

Base: `origin/neoforge-26.2` (`eebffce`) plus the reviewed keybinding-category fix.

## Pinned dependencies (verified 2026-09-25 UTC)

- Minecraft 26.3 (release, published 2026-09-15T11:23:02Z).
- NeoForge `26.3.0.22-beta` — latest 26.3 entry in
  `https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml`.
- **NeoGradle `7.1.39`** (was 7.1.38). Required: NeoForge >= 26.2.0.87 ships an
  access transformer that makes older NeoGradle fail while recompiling patched
  `HolderSet.java` ("contents() ... cannot override", NeoForge issues #3490 /
  #3518). With 7.1.38 `:neoFormRecompile` aborts before our code is compiled.
- Optional integrations stay **reflection-based**, so they are not compile or
  published dependencies. Local test jars are ignored by git (`libs/`), not vendored:

| Jar | Source | SHA-1 |
|---|---|---|
| `trade-cycling-neoforge-1.0.22+26.3.jar` | Modrinth `eYmTi0EJ` | `ff375f4c53eaff99faa5db734079f3dca0554812` |
| `easy-villagers-neoforge-1.1.43+26.3.jar` | Modrinth `CdpNBYv0` | `604f49e2d5fca0f7a2fc78be93eff814da69920e` |

Superseded 26.2 dev-compile stub preserved at
`../_deps-26.3/superseded/easy-villagers-neoforge-1.1.42+26.2.jar`.

## Source changes for 26.3

26.3 removed the GLFW keyboard dependency and `InputConstants.Type.KEYSYM`.

- `Keybindings.java`: `InputConstants.Type.KEYBOARD` with native
  `InputConstants.KEY_R` / `KEY_C`; the mod category is now registered explicitly
  through `RegisterKeyMappingsEvent#registerCategory` instead of the deprecated
  `KeyMapping.Category.register(Identifier)`.
- `InputHandler.java`: key-press test uses `InputConstants.PRESS` instead of
  `GLFW.GLFW_PRESS`; `KeyMapping#matches(KeyEvent)` now also handles unbound keys.
- `build.gradle`: NeoGradle 7.1.39; blocked-guard removed; dependency wiring
  replaced by absent-tolerant local test jars; optional dev-only
  `-PquickPlayWorld=<name>` launch argument for manual testing.
- `.gitignore`: `libs/` (test jars are not vendored).

Nothing else was touched. `runs/`, filters and existing local gitignores are intact.

## Validation performed

`./gradlew --no-daemon --max-workers=2 clean build` → BUILD SUCCESSFUL.

Real client launches on a **private headless Xvfb display** (`:99`, llvmpipe), so no
live desktop session was used:

1. Dev client reached the main menu with 4 mods loaded; `easyautocycler.mixins.json`
   was prepared and `Registered key mappings` logged.
2. Quick-played into a flat test world; the mixin applied:
   `Mixing ClientPacketListenerMixin from easyautocycler.mixins.json into
   net.minecraft.client.multiplayer.ClientPacketListener`, mapping to 26.3's real
   `handleMerchantOffers` (no missing-mixin error), and
   `Trade Cycling mod is loaded` / `Trade Cycling support enabled`.
3. Spawned beside a villager trader and opened its trade screen (client produced the
   real MerchantScreen; "Apprentice" observed).
4. Pressing `R` in the trade screen exercised the full path:
   `--- Toggle Key Pressed (MerchantScreen)! ---`,
   `Starting network-synchronized villager trade cycling.` and
   `Auto-cycling started. Press button again to stop.`

Evidence screenshots: `../_evidence-26.3/` (main menus, world loaded, trader screen).
Logs: `../neoforge-26.3-runclient-world5.log`, `../neoforge-26.3-build-26.3.log`.

## Known limitations — do not overclaim

- ~~**The cycle round-trip was not confirmed.**~~ **RESOLVED — see the follow-up section
  below.** The first cycle timed out:
  `No merchant-offers acknowledgement received after 100 ticks` →
  `Stopping villager trade cycling. Reason: Merchant offers update timed out`. `R`
  is detected and cycling starts. The cause was the test villager having no claimed
  workstation; with one present, 3000 cycles round-trip and a filter find-and-stop
  works. Trade Cycling's own in-screen cycle button was never needed to settle this —
  the decompiled server path settles it. Only the real-GPU client re-test stays open.
- **The clean-build claim is narrower than it sounds.** `clean build` passed while
  NeoForm's cached vanilla recompilation was still present; deleting the NeoForm
  cache forces a full ~7,000-file recompile. That path succeeded earlier in this
  session with 7.1.38 (failing only on `HolderSet`, which 7.1.39 fixes), but a
  from-empty-cache build has not been re-run end to end with the final files.
- ~~**Dedicated servers cannot load this mod.**~~ **RESOLVED — see the follow-up section
  below.** `./gradlew runServer` formerly failed with
  `NoClassDefFoundError: net/minecraft/client/resources/sounds/SoundInstance` because the
  common entrypoint registered client listeners. Fixed by splitting the entrypoint; the
  server now starts and no-ops cleanly.
- Running the UI on llvmpipe with no sound device produced `Failed to open OpenAL
  device` and a `minecraft:end_of_frame` post-effect warning; both are environmental,
  not mod defects. Blur was not exercised (no world blur on these screens).
- No 1.21.1-era blur rule applies: these screens draw in one `extractRenderState`
  pass with no manual `renderBackground` call.

Before enabling any release target: re-check upstream 26.3 artifacts and complete a
real (GPU) client test.

## Follow-up 2026-09-25 (2) — both blockers diagnosed, cycling root cause confirmed

### Dedicated-server crash: fixed by splitting the entrypoint

`NoClassDefFoundError: net/minecraft/client/resources/sounds/SoundInstance` was reproduced
and fixed. Cause: `EasyAutoCyclerMod` was the *common* entrypoint and registered three
listener method references, two of which (`clientSetup`, `registerKeybindings`) reach
client-only code (`ClientEventHandler`, `InputHandler`, `Keybindings`, `AutomationManager`).
Verifying those references on a dedicated server made the loader resolve `SoundInstance`,
which does not exist there, aborting startup.

`EasyAutoCyclerMod` now registers only `commonSetup` and references no `net.minecraft.client.*`
type. Client wiring moved to a second, client-only entrypoint
`com.uncraftbar.easyautocycler.EasyAutoCyclerClientMod` annotated
`@Mod(value = MODID, dist = Dist.CLIENT)`. `AutomationManager.initialize()` moved from
common setup to client setup, matching its client-only state (current screen, sound manager).

### Cycling timeout: root cause is the villager, not the mod

Decompiled `trade-cycling-neoforge-1.0.22+26.3.jar` (Vineflower 1.11.1) and read the server
side. `TradeCyclingMod.onCycleTrades` sends `player.sendMerchantOffers(...)` only when all of:

- the player's open menu is a `MerchantMenu`,
- `getTraderXp() <= 0` **or** `MerchantContainer#getActiveOffer()` is null, and
- **`villager.getBrain().getMemory(MemoryModuleType.JOB_SITE)` is non-empty.**

If any condition fails it `return`s silently — no log line, no packet. The test villager was
`/summon`ed at `0.5, -60.0, 4.5` with `NoAI: 1`, profession `minecraft:weaponsmith`, level 2,
`Xp: 0`, two offers, and an **empty `Brain.memories`**: no claimed workstation. The server
therefore accepted the cycle packet and dropped it, which is exactly the observed
"no acknowledgement after 100 ticks" with no error on either side.

`CAN_CYCLE` is not the problem: client-side `CycleTradesButton.canCycle` is
`menu.showProgressBar() && menu.getTraderXp() <= 0`, and `AbstractVillager.showProgressBar()`
returns a constant `true`, so `R` does start cycling. The gap is server-side only.

`EasyAutoCyclerMod` now carries a DEBUG (not TRACE) acknowledgement line so an actual
round-trip is visible in console logs, and the timeout message names the likely cause.

### Verified end-to-end cycling (local dev client, Xvfb :99, llvmpipe)

With a claimed workstation present, cycling completes. Test setup was done **in-game**,
so the evidence is real server state, not a mock:

1. `/setblock 1 -61 4 minecraft:grindstone` (weaponsmith job-site POI).
2. `/data modify entity @e[type=minecraft:villager,limit=1,sort=nearest]
   Brain.memories."minecraft:job_site" set value
   {value:{dimension:"minecraft:overworld",pos:[I;1,-61,4]}}`
   — verified with `/data get`; it also survives a world save.
3. Right-click the villager, press `R`.

Results, all from `runs/client-trade-cycling/logs/`:

| Check | Result |
|---|---|
| Cycle round-trip (no-match filter) | `Received merchant-offers acknowledgement for cycle N` x3000 |
| Find-and-stop (`bell`, ≤64 emeralds) | `Target trade found: bell  •  ≤64 emeralds` within 3 ms, then stopped |
| No false positive (`diamond` x64, ≤1 emerald) | 3000 cycles, never reported a find |
| Pacing after the fix | 119 acknowledgements in 13.8 s (8.6 cycles/s, ~2 ticks/cycle) |
| Dedicated server | `Done (0.449s)! For help, type "help"`, no `NoClassDefFoundError` |

The previous "no acknowledgement" failure was **not** a mod or Trade Cycling defect: the
test villager simply had no claimed workstation, exactly as in the scenario above with the
memory removed.

### Cycle pacing fix (found while validating the above)

Replacing the old fixed "click delay" with acknowledgement-driven stepping removed all
pacing: on a local server the acknowledgement lands in the same tick, so the loop ran
**3000 cycles in ~0.4 s** (~7000/s) and burned the entire `MAX_CYCLES_SAFETY` budget almost
instantly, making the safety limit meaningless and spamming the server. Added
`MIN_CYCLE_DELAY_TICKS = 2` (the previous default click delay), applied from the moment an
acknowledgement is received. Measured after the fix: ~2 ticks/cycle, so the 3000-cycle limit
now corresponds to about 5 minutes of real cycling.

Reproduced evidence kept outside the worktree (relative to this directory):

| File | Content |
|---|---|
| `../neoforge-26.3-cyclefix-runA-3000-acks-nomatch.log` | 3000 acknowledgements in ~0.4 s, pre-fix, no false-positive find |
| `../neoforge-26.3-cyclefix-runB-bell-found.log` | `Target trade found: bell  •  ≤64 emeralds` then stop |
| `../neoforge-26.3-cyclefix-runC-pacing-debug.log` | post-fix pacing, cycles 1..120 over 13.8 s |
| `../_evidence-26.3/neoforge-26.3-cyclefix-merchant-screen-with-jobsite.png` | open trade screen used for the test |

Note the run order: Run A (no-match filter) ran **before** the pacing fix, Run B (bell
filter) also ran before it, and Run C (pacing) ran after.

### Test-environment notes (not mod defects)

- `/time query daytime` errors on 26.3 (`Can't find element 'minecraft:daytime' of type
  'minecraft:timeline'`); 26.3 moved day time to the timeline system. Unrelated to this mod.
- `cycworld` had `allowCommands=0`, so the dev player was not op and every non-`/help`
  command failed with "Unknown or incomplete command". Cheats were enabled locally for
  testing only, via a byte patch of `level.dat`. The original is preserved at
  `/tmp/cycworld-level-orig.dat` and the whole world at `/tmp/cycworld-backup-234604/`.
- `runs/client-trade-cycling/ops.json` was added locally as an op fallback; it did not take
  effect because `isOp` is gated on `isSingleplayerOwner`, so the `level.dat` patch above is
  what actually enabled commands.

## Follow-up 2026-09-26 — Easy Villagers path tested end to end; real defect found

The earlier cycling evidence came only from the **Trade Cycling** path. Easy Villagers uses a
different server handler (see below), so it was exercised separately.

### Setup

Dev client on a private headless display `:98` (llvmpipe), Easy Villagers `1.1.43+26.3` as the only
integration (`-PruntimeEasyVillagers`), game dir `runs/client-easy-villagers/`, reusing the
`cycworld` test world. A `/summon`ed weaponsmith with `Brain.memories."minecraft:job_site"` set to a
grindstone at `1,-61,4` sits beside the player.

### What was exercised

1. `R` in the open trade screen: `--- Toggle Key Pressed (MerchantScreen)! ---`, then
   `Auto-cycling started. Press button again to stop.`
2. The filter `bell • ≤64 emeralds` immediately reported
   `Target trade found: bell  •  ≤64 emeralds` and stopped.
3. `/data get entity @e[type=minecraft:villager,limit=1,sort=nearest] Offers` **before** starting
   and again **after** the run returned byte-identical data:

   ```
   {Recipes: [{maxUses: 12, buy: {count: 4, id: "minecraft:iron_ingot"},
               sell: {count: 1, id: "minecraft:emerald"}, xp: 10,
               specialPrice: 10, priceMultiplier: 0.05f},
              {maxUses: 12, buy: {count: 36, id: "minecraft:emerald"},
               sell: {count: 1, id: "minecraft:bell"}, xp: 5,
               specialPrice: 40, priceMultiplier: 0.2f}]}
   ```

   No trade changed, yet a match was reported.

### Root cause (corrected): a null dereference at start, not an Easy Villagers reroll bug

A first pass blamed an Easy Villagers "acknowledges once per stack" reroll quirk. **That was
wrong** and the log disproves it: the run recorded **0 acknowledgement lines and 0 timeouts**. The
match was reported **before any cycle request was sent**:

```
--- Toggle Key Pressed (MerchantScreen)! ---
Starting network-synchronized villager trade cycling.
Auto-cycling started. Press button again to stop.
Target trade FOUND using filter!
Target trade found: bell  •  ≤64 emeralds
Stopping villager trade cycling. Reason: Target trade found with filter
```

`AutomationManager.start()` calls `evaluateAndMaybeCycle(...)` immediately, which evaluates the
**current** offers and stops. On that path `lastMatchedFilter` had never been set, because
`checkTradesWithFilters` only assigns it when `matchAny` is true:

```java
if (matchAny) { ... this.lastMatchedFilter = filter; return true; }
else { ... this.lastMatchedFilter = enabledFilters.get(0); return true; }
```

...but the caller built the chat message **before** calling it:

```java
} else if (enabledFilters.isEmpty()) {                       // AND mode reaches here
    if (checkTradesForEnchantment(...)) { ... }              // does not set lastMatchedFilter
    else if (checkTradesForItem(...)) { ... }                // does not set lastMatchedFilter
}
```

With `matchAny = false` the enchantment/item branch returns `true` while `lastMatchedFilter` is
still `null`, and `lastMatchedFilter.getDisplayName()` throws on the render thread. The mod treats
`merchant_offers` as missing `runOnNextTick` if it crashed there; either way the client dies
silently — no crash report, no FATAL line, just a frozen window. That is exactly what run 1 showed.

Two defects were therefore fixed:

1. **Null dereference.** `checkFiltersAndReport(...)` now reads `lastMatchedFilter` into a local
   only *after* the check returns true, and falls back to a literal label if it is still null, so
   a reporting path cannot throw.
2. **Wasted reroll on an already-matching trade.** `start()` now evaluates the current offers
   *before* sending any cycle request. A trade that already satisfies the filter is reported as-is;
   cycling it would destroy the very trade the player pressed the key to keep.

`evaluateAndMaybeCycle` and `start()` now share the one `checkFiltersAndReport` path so the two
cannot drift apart again.

### Status — fixed in source; Easy Villagers re-verification incomplete

Committed in `AutomationManager`. Re-tested on the Easy Villagers path after the fix:

| Check | Result |
|---|---|
| No-match filter (`diamond` x64, ≤1 emerald) | **264 acknowledgements, 0 false finds**, ~2 ticks/cycle |
| Compile + startup after fix | clean; Easy Villagers support enabled |
| Match filter (`bell`, ≤64 emeralds) end to end | **not re-confirmed** — the automated `R` press landed after the run was torn down |

The crash path is fixed (the throwing expression is gone and no-match cycling is clean), but the
specific "trade already matches when `R` is pressed" outcome still needs one manual click-through.
Do not read this as a full Easy Villagers sign-off.

Still open, unrelated to the null dereference:

- The timeout message names a missing workstation, a check only Trade Cycling performs. Make it
  integration-specific.
- The "found" report does not say which trade slot matched.
- No real-GPU visual pass yet.

### Evidence

| File | Content |
|---|---|
| `../neoforge-26.3-easyvillagers-e2e-run1.log` | full run: mod detection, toggle, match report, both `/data get` dumps |
| `../neoforge-26.3-easyvillagers-e2e-run1-keylines.txt` | key lines extracted |
| `../neoforge-26.3-easyvillagers-e2e-run2-nomatch-pacing.log` | post-fix: 264 acks, no false find, ~2 ticks/cycle |
| `runs/client-easy-villagers/crash-reports/crash-2026-09-26_02.28.28-client.txt` | environmental SDL failure (no display), no mod frames — not a mod defect |
| `../_evidence-26.3/2026-09-26_02.18.45.png` | in-game F2 screenshot of the trade screen after `R` |
