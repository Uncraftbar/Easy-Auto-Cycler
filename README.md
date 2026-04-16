# Easy Auto Cycler

[![CurseForge Downloads](https://cf.way2muchnoise.eu/full_1242344_downloads.svg?badge)](https://curseforge.com/minecraft/mc-mods/easy-auto-cycler)
[![Modrinth](https://img.shields.io/badge/dynamic/json?labelColor=black&color=grey&label=&suffix=%20downloads&query=downloads&url=https://api.modrinth.com/v2/project/easy-auto-cycler&style=flat&logo=modrinth)](https://modrinth.com/mod/easy-auto-cycler)
[![GitHub license](https://img.shields.io/github/license/Uncraftbar/Easy-Auto-Cycler)](LICENSE)

> **Branch: `neoforge-26.1.2`** — NeoForge 26.1.2 with [Easy Villagers](https://www.curseforge.com/minecraft/mc-mods/easy-villagers) and [Trade Cycling](https://www.curseforge.com/minecraft/mc-mods/trade-cycling) support.

A powerful client-side Minecraft mod that automates villager trade cycling. Set up filters for the trades you want, press a keybind, and the mod automatically re-rolls until it finds a match.

## Features

- **Dual-Mod Support:** Works with either [Easy Villagers](https://www.curseforge.com/minecraft/mc-mods/easy-villagers) or [Trade Cycling](https://www.curseforge.com/minecraft/mc-mods/trade-cycling) — only one is required
- **Advanced Filter System:** Multiple filters with enchantment, item, payment, and price criteria
- **Flexible Logic:** AND/OR mode for combining filters
- **Session Persistence:** Filters save and restore automatically between sessions
- **Auto-Stop:** Cycling stops when a matching trade is found
- **Toggle Keybind:** Start/stop with `R` (configurable)
- **Config GUI:** Press `C` or click ⚙ in the trade screen to manage filters
- **Intuitive GUI:** Autocomplete for enchantment and item IDs
- **Lightweight & Client-Side:** No server installation required

## Requirements

- **Minecraft:** 26.1.2
- **Mod Loader:** [NeoForge](https://neoforged.net/) 26.1.2+
- **Java:** 25
- **One of:**
  - [Easy Villagers](https://www.curseforge.com/minecraft/mc-mods/easy-villagers)
  - [Trade Cycling](https://www.curseforge.com/minecraft/mc-mods/trade-cycling)

## Usage

1. **Open a villager trade screen** (the cycle button from Easy Villagers or Trade Cycling must be available)
2. **Configure filters:** Press `C` or click ⚙ → Add Filter → set your criteria
3. **Start cycling:** Press `R` or click ▶
4. **Stop:** Press `R` again, or wait for auto-stop when a match is found

### Filter Options

| Field | Description |
|-------|-------------|
| Enchantment ID | Target specific enchantment (e.g., `minecraft:mending`) |
| Min Enchantment Level | Minimum level required |
| Item ID | Target specific item (e.g., `minecraft:diamond_sword`) |
| Min Count | Minimum quantity of the item |
| Payment Item | Custom payment item (leave empty for emeralds) |
| Max Payment Cost | Maximum cost willing to pay |

## Other Versions

| Branch | Loader | MC Version | Supported Mods |
|--------|--------|------------|----------------|
| **`neoforge-26.1.2`** | **NeoForge** | **26.1.2** | **Easy Villagers, Trade Cycling** |
| [`fabric-26.1.2`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/fabric-26.1.2) | Fabric | 26.1.2 | Trade Cycling |
| [`neoforge-1.21.1`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/neoforge-1.21.1) | NeoForge | 1.21.1 | Easy Villagers, Trade Cycling |
| [`fabric-1.21.1`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/fabric-1.21.1) | Fabric | 1.21.1 | Trade Cycling |
| [`forge-1.20.1`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/forge-1.20.1) | Forge | 1.20.1 | Easy Villagers, Trade Cycling |
| [`fabric-1.20.1`](https://github.com/Uncraftbar/Easy-Auto-Cycler/tree/fabric-1.20.1) | Fabric | 1.20.1 | Trade Cycling |
