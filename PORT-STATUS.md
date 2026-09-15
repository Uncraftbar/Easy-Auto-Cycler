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
- No old Trade Cycling 26.2 jar: integration is reflection-based and the
  optional runtime file ID is blank until a compatible artifact is verified.

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

`./gradlew --no-daemon --max-workers=2 clean build` **passed**, 15 seconds,
6 executed tasks (compileJava, resources, jar and remapJar included). Java25
toolchain used. `test NO-SOURCE`: there is no unit test suite; a build is not a
functional gameplay test. Earlier attempts hit a download failure, then exposed
12 API compile errors; both were addressed before the successful clean build.

Build log: sibling `../fabric-26.3-build-final.log`.
Artifact: `build/libs/easyautocycler-fabric-26.3-3.1.2-local.26.3.jar`.
No client launch, Controls screen screenshot, GUI rendering/blur check, trading
test or networking integration acceptance was performed. Trade Cycling has no
26.3 listing on queried Modrinth metadata: **end-to-end functionality and public
release remain gated on a compatible integration artifact and client testing**.
Do not force-load a 26.2 integration jar or describe this as runtime-verified.
