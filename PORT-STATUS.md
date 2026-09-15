# NeoForge 26.3 — blocked preparation, not a supported port

Checked 2026-09-15 UTC. Minecraft **26.3 release** exists (Mojang release time
11:23:02Z), but NeoForge Maven metadata contains **no 26.3 artifact**; latest
listed version is 26.2.0.88. No version is invented and no 26.2 build is relabelled.

Base: origin/neoforge-26.2 `b471419`, plus pending PR #39 keybinding category fix.
The local branch reserves 26.3 metadata but intentionally fails at configuration
with a clear error until real loader and integration dependencies are available.
Inherited integration coordinates in build.gradle remain **26.2 reference only**
behind this guard; they must be replaced before removing the guard.

Upstream evidence:
- https://piston-meta.mojang.com/mc/game/version_manifest_v2.json
- https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml
- https://api.modrinth.com/v2/project/trade-cycling/version?game_versions=%5B%2226.3%22%5D (empty)
- https://api.modrinth.com/v2/project/easy-villagers/version?game_versions=%5B%2226.3%22%5D (empty)

No loadable artifact, runtime test, or support claim. Recheck upstream, pin the
actual NeoForge and optional integration artifacts, adapt APIs, build, and run
client trade/GUI tests before enabling any release target. A Gradle build failure
at the guard is expected, not successful port validation.

Observed local `./gradlew --no-daemon --max-workers=2 build` failed in 27s with
the explicit guard message (and a secondary NeoGradle missing-toolchain message
because configuration was deliberately interrupted). Log:
`../neoforge-26.3-build.log`. No jar was produced.
