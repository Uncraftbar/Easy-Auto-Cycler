# v3.1.2 — Minecraft 26.3 support & keybinding category fixes

## New: Minecraft 26.3

- Added **NeoForge 26.3** support for **Easy Villagers** and **Trade Cycling**.
- Added **Fabric 26.3** support for **Trade Cycling**. Easy Villagers has no Fabric version.
- Updated keybindings and input handling for Minecraft 26.3's input APIs.
- Fixed clicking autocomplete suggestions on both loaders, and fixed Tab, arrow-key, and Enter completion on NeoForge.
- Restored a minimum two-tick pause between acknowledged cycles, preventing instant exhaustion of the cycle safety limit on low-latency servers.
- Fixed matched-filter reporting, including AND-mode matches; NeoForge also checks current offers before starting another cycle.
- Isolated NeoForge's client initialization so an accidental dedicated-server installation no longer loads client-only classes. Easy Auto Cycler is still client-side and does not need to be installed on servers.

Use **Java 25** for Minecraft 26.3. Tested with NeoForge **26.3.0.22-beta**, Easy Villagers **1.1.43+26.3**, Trade Cycling **1.0.22+26.3**, Fabric Loader **0.19.5**, and Fabric API **0.160.5+26.3**. Install the matching 26.3 integration mod; Fabric also requires Fabric API. The integration mod's own server requirements still apply.

## Keybinding category correction on existing versions

The Controls screen now shows the translated **Easy Auto Cycler** category instead of the raw translation key. Includes the fixes from PRs **#34–#39** for **Fabric and NeoForge on Minecraft 1.21.11, 26.1.2, and 26.2**. These six builds contain the category correction only; the 26.3-specific changes above are not backported in this release.

## Release scope

This release contains **eight JARs**: Fabric and NeoForge for **1.21.11, 26.1.2, 26.2, and 26.3**. Choose the JAR matching your Minecraft version and loader.

**No new builds for 1.20.1 or 1.21.1:** those branches have no unpublished changes and retain their existing releases (Fabric 1.20.1: 3.1.1; the other unchanged targets: 3.1.0).
