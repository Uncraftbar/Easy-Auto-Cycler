# Testing the local NeoForge 26.3 build

Local-only instructions. Nothing here publishes, tags or uploads anything.

## What to test with

| Component | Version |
|---|---|
| Minecraft | 26.3 |
| NeoForge | `26.3.0.22-beta` |
| Easy Villagers | `1.1.43+26.3` (Modrinth `CdpNBYv0`) |
| Trade Cycling | `1.0.22+26.3` (Modrinth `eYmTi0EJ`) |
| Java | 25 |

The two integration mods are **optional and mutually independent** — you need only one. Both are
supported at runtime; the mod detects which one is present. Test each separately so it is clear
which path is being exercised.

## Path A — PrismLauncher (normal user setup)

1. Create a Prism instance: **Minecraft 26.3** + **NeoForge 26.3.0.22-beta**, Java 25.
2. Drop the built jar into the instance's `minecraft/mods/`:

   ```
   build/libs/easyautocycler-neoforge-26.3-3.1.2-local.26.3.jar
   ```

3. Add **one** of the integration jars to the same `mods/` folder:
   - `libs/easy-villagers-neoforge-1.1.43+26.3.jar`, or
   - `libs/trade-cycling-neoforge-1.0.22+26.3.jar`

   (These are local test copies and are gitignored. They are not bundled with the mod and are not
   required at build time.)
4. Launch, open a villager trade screen, and press `R`.

### Important: the villager must have a **claimed workstation**

Both integration mods **silently** drop a cycle request when the villager has no claimed job-site
block: no log line, no packet. A `/summon`ed villager with `NoAI:1` has an empty
`Brain.memories`, so cycling will just time out. This is upstream behaviour, not a mod defect.

Fix it in-game (creative + cheats):

```
/setblock <x> <y> <z> minecraft:grindstone
/data modify entity @e[type=minecraft:villager,limit=1,sort=nearest] \
  Brain.memories."minecraft:job_site" set value \
  {value:{dimension:"minecraft:overworld",pos:[I;<x>,<y>,<z>]}}
```

Confirm with `/data get entity @e[type=minecraft:villager,limit=1,sort=nearest] Brain.memories`,
then close and reopen the trade screen.

## Path B — Gradle dev client (developer loop)

```bash
cd /home/Uncraftbar/Projects/easy-auto-cycler-worktrees/neoforge-26.3

# Trade Cycling only
./gradlew --no-daemon runClient -PruntimeTradeCycling -PquickPlayWorld=cycworld

# Easy Villagers only
./gradlew --no-daemon runClient -PruntimeEasyVillagers -PquickPlayWorld=cycworld

# both integrations at once
./gradlew --no-daemon runClient -PruntimeBothIntegrations -PquickPlayWorld=cycworld
```

`-PquickPlayWorld=<world>` is a dev-only convenience that jumps straight into a world.
Each integration gets its own game directory under `runs/` (`client-trade-cycling`,
`client-easy-villagers`, `client-both`), so filters and worlds do not collide.

Headless machines: this was tested on a private Xvfb display, never on a desktop session.

```bash
Xvfb :98 -screen 0 1280x800x24 -nolisten tcp &
export DISPLAY=:98 LIBGL_ALWAYS_SOFTWARE=1
./gradlew --no-daemon runClient -PruntimeEasyVillagers -PquickPlayWorld=cycworld
```

## Building the jar

```bash
./gradlew --no-daemon --max-workers=2 clean build
```

## Reading the result

Cycling is only really proven by the acknowledgement line, which is logged at DEBUG:

```
Received merchant-offers acknowledgement for cycle N
```

In the dev run configs the console level is `debug`, so it appears. A filter hit shows up as:

```
[CHAT] Target trade found: <filter name>
```

If you instead see `Timed out waiting for updated villager trades`, check the workstation first.

## Known caveats (read before judging a result)

- **Easy Villagers false positive (open).** Easy Villagers re-sends an *identical* offer set when
  the villager has a single possible trade outcome. The filter is then evaluated on that unchanged
  list, so a matching trade can be reported as "found" even though nothing was re-rolled. Trade
  Cycling does not show this. See PORT-STATUS.md.
- Rendering was only ever checked on llvmpipe (software GL). A real-GPU visual pass is still open.
- `Failed to open OpenAL device` and the `minecraft:end_of_frame` post-effect warning in headless
  runs are environmental, not mod defects.
- The `3.1.2-local.26.3` version string is a local candidate identifier, not a release.
