# Fabric 26.3 local port — 2026-09-15

Target: **Minecraft 26.3 release**, published by Mojang at 11:23:02Z today.
Not 26.2, not a prerelease. Base origin/fabric-26.2 `08d3ac1` plus pending
PR #38 category translation correction. Version `3.1.2-local.26.3` is a local
candidate only; no push, release, tags or publisher tasks.

## Pinned dependencies

- Minecraft 26.3, official unobfuscated classes (empty identity Tiny mapping).
- Fabric Loader 0.19.5 stable.
- Fabric API reference aggregate 0.160.5+26.3; selected modules:
  base 2.0.6+fcdff87f02, key mapping 2.0.8+3434d6d902,
  screen 5.2.3+1087e13e02, networking 6.3.8+fcdff87f02,
  resource loader **v1** 3.0.4+fcdff87f02.
- Loom 1.17.0-alpha.5, Gradle 9.4.0, Java toolchain/release 25.
- Trade Cycling **Fabric 1.0.22+26.3 now exists** (Modrinth `URzCi2W4`,
  SHA-1 `c23a191f2f9c1c7d685effaf6ba656076c88bb77`, listed 2026-09-19). It is wired
  in as a local test-jar runtime input only (`libs/trade-cycling-fabric-1.0.22+26.3.jar`,
  gitignored, **not vendored**). Integration stays reflection-based, so the jar is
  neither a compile nor a published dependency.

Sources queried on 2026-09-15:
- https://piston-meta.mojang.com/mc/game/version_manifest_v2.json
- https://meta.fabricmc.net/v2/versions/loader/26.3
- https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.160.5+26.3/fabric-api-0.160.5+26.3.pom
- https://api.modrinth.com/v2/project/trade-cycling/version?game_versions=%5B%2226.3%22%5D (empty)

## API changes and validation

26.3 removed the GLFW keyboard dependency and Type.KEYSYM. Use
InputConstants.Type.KEYBOARD and native InputConstants.KEY_* values, not old
GLFW integer codes. Merchant screen toggle now delegates to KeyMapping.matches,
which also handles unbound keys correctly. Suggestion navigation/acceptance uses
the 26.3 constants including KEY_RETURN and KEY_NUMPADENTER.

Confirmed against javap of the actual 26.3 official client:
SHA1 e877b6a07acd633fb3bb475002175cec036e7b87. InputConstants.getKey and
KeyMapping.matches use KEYBOARD and KeyEvent.key(), matching this port.

`./gradlew --no-daemon --max-workers=2 clean build` **passed**, 13 seconds,
6 executed tasks (compileJava, resources, jar and remapJar included). Java25
toolchain used. `test NO-SOURCE`: there is no unit test suite; a build is not a
functional gameplay test. Earlier attempts hit a download failure, then exposed
12 API compile errors; both were addressed before the successful clean build.

Build log: sibling `../fabric-26.3-build-26.3.log` (earlier run:
`../fabric-26.3-build-final.log`).
Artifact: `build/libs/easyautocycler-fabric-26.3-3.1.2-local.26.3.jar`.

## Client launch (2026-09-25, re-verified)

Launched the dev client on a private headless Xvfb display (`:99`, llvmpipe) with the
pinned Trade Cycling 26.3 jar as a runtime input. The client reached the main menu and
loaded the mod alongside `trade_cycling 1.0.22+26.3`:

```
EasyAutoCyclerMod loaded! (Fabric)
Trade Cycling mod is loaded
Trade Cycling support enabled
Registered key mappings
```

## Client gameplay test (2026-09-26) — trade cycling verified end-to-end

The full round-trip now runs on the Fabric port. Dev client launched on a private
headless Xvfb display `:100` (llvmpipe) with the pinned Trade Cycling 26.3 jar, quick-played
straight into a copy of the NeoForge test world. The villager's workstation was claimed
in-game, so this is real server state:

1. `/gamemode creative`
2. `/setblock 1 -61 4 minecraft:grindstone`
3. `/data modify entity @e[type=minecraft:villager,limit=1,sort=nearest]
   Brain.memories."minecraft:job_site" set value
   {value:{dimension:"minecraft:overworld",pos:[I;1,-61,4]}}` — confirmed by `/data get`
4. right-click the villager, press `R`

Measured result (log slice `../fabric-26.3-cyclefix-runA-96-acks.log`):

| Check | Result |
|---|---|
| Cycle round-trip | `Received merchant-offers acknowledgement for cycle N` x96 |
| Pacing | 96 acknowledgements in 12 s (~8 cycles/s, ~2 ticks/cycle) — pacing floor active |
| Toggle | `Auto-cycling started` → `Stopping villager trade cycling. Reason: Toggled off by user` |
| Filtering / find-and-stop | **NOT re-tested on Fabric** this session (same code path as NeoForge, which passed) |
| Max-cycles safety limit | not reached in this run (96 of 3000) |

Evidence: `../_evidence-26.3/fabric-26.3-merchant-screen-with-jobsite.png`,
`../_evidence-26.3/fabric-26.3-cycling-active.png`,
`../fabric-26.3-runclient-verified.log`.

### Cycle pacing fix (same defect as NeoForge)

The Fabric port carried the identical zero-pacing defect: acknowledgement-driven
stepping with no tick floor, so a local server acknowledges within the same tick and the
loop burns `MAX_CYCLES_SAFETY` almost instantly. Applied the same fix as the verified
NeoForge port — `MIN_CYCLE_DELAY_TICKS = 2` (the previous default click delay), rearmed
on each acknowledgement — plus the clearer timeout message and DEBUG (not TRACE)
acknowledgement line. This is the only source change in this session.

### Still not performed

- Controls-screen screenshot and the GUI/blur visual check.
- Find-and-stop with a real filter on Fabric (NeoForge equivalent passed).
- Real-GPU client test (all runs are llvmpipe software rendering).
- Dedicated-server run: not applicable to Fabric, whose `fabric.mod.json` is already
  `"environment": "client"`.

Public release remains gated on those. Do not describe this port as fully verified, and
do not publish the local-suffixed version.
