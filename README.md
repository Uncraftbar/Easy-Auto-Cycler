# Easy Auto Cycler

[![CurseForge Downloads](https://cf.way2muchnoise.eu/full_1242344_downloads.svg?badge)](https://curseforge.com/minecraft/mc-mods/easy-auto-cycler)
[![Modrinth](https://img.shields.io/badge/dynamic/json?labelColor=black&color=grey&label=&suffix=%20downloads&query=downloads&url=https://api.modrinth.com/v2/project/easy-auto-cycler&style=flat&logo=modrinth)](https://modrinth.com/mod/easy-auto-cycler)
[![GitHub license](https://img.shields.io/github/license/Uncraftbar/Easy-Auto-Cycler)](LICENSE)

A powerful client-side Minecraft mod that automates villager trade cycling. Set up filters for the trades you want, press a keybind, and the mod automatically re-rolls until it finds a match. Works with [Easy Villagers](https://www.curseforge.com/minecraft/mc-mods/easy-villagers) and [Trade Cycling](https://www.curseforge.com/minecraft/mc-mods/trade-cycling).

## Features

- **Dual-Mod Support:** Works with Easy Villagers or Trade Cycling — only one is required
- **Advanced Filter System:** Multiple filters with enchantment, item, payment, and price criteria
- **Flexible Logic:** AND/OR mode for combining filters
- **Session Persistence:** Filters save and restore automatically between sessions
- **Auto-Stop:** Cycling stops when a matching trade is found
- **Toggle Keybind:** Start/stop with `R` (configurable)
- **Config GUI:** Press `C` or click ⚙ in the trade screen to manage filters
- **Lightweight & Client-Side:** No server installation required

## Supported Versions

| Branch | Loader | MC Version | Supported Mods |
|--------|--------|------------|----------------|
| [`neoforge-26.2`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/neoforge-26.2) | NeoForge | 26.2 | Easy Villagers, Trade Cycling |
| [`fabric-26.2`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/fabric-26.2) | Fabric | 26.2 | Trade Cycling |
| [`neoforge-26.1.2`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/neoforge-26.1.2) | NeoForge | 26.1.2 | Easy Villagers, Trade Cycling |
| [`fabric-26.1.2`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/fabric-26.1.2) | Fabric | 26.1.2 | Trade Cycling |
| [`neoforge-1.21.11`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/neoforge-1.21.11) | NeoForge | 1.21.11 | Easy Villagers, Trade Cycling |
| [`fabric-1.21.11`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/fabric-1.21.11) | Fabric | 1.21.11 | Trade Cycling |
| [`neoforge-1.21.1`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/neoforge-1.21.1) | NeoForge | 1.21.1 | Easy Villagers, Trade Cycling |
| [`forge-1.20.1`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/forge-1.20.1) | Forge | 1.20.1 | Easy Villagers, Trade Cycling |
| [`fabric-1.21.1`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/fabric-1.21.1) | Fabric | 1.21.1 | Trade Cycling |
| [`fabric-1.20.1`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/fabric-1.20.1) | Fabric | 1.20.1 | Trade Cycling |

> **Note:** Easy Villagers is Forge/NeoForge only. Fabric builds support Trade Cycling only.

For downloads, check the [Releases](https://github.com/Uncraftbar/Easy-Auto-Cycler/releases) page, [CurseForge](https://curseforge.com/minecraft/mc-mods/easy-auto-cycler), or [Modrinth](https://modrinth.com/mod/easy-auto-cycler).

## Quick Start

1. Install the mod + either Easy Villagers or Trade Cycling
2. Open a villager trade screen
3. Press `C` or click ⚙ to configure filters
4. Press `R` or click ▶ to start cycling
5. The mod stops automatically when a match is found

See each branch's README for version-specific details.

## Publishing releases

The manually triggered **Publish release** GitHub Actions workflow builds every supported branch and publishes matching releases to Modrinth and CurseForge. It defaults to a safe dry run; see [`release/README.md`](release/README.md) for token setup and the release process.
