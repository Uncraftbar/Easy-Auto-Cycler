# Easy Auto Cycler

[![CurseForge Downloads](https://cf.way2muchnoise.eu/full_1242344_downloads.svg?badge_style=flat)](https://curseforge.com/minecraft/mc-mods/easy-auto-cycler)
[![GitHub license](https://img.shields.io/github/license/Uncraftbar/Easy-Auto-Cycler)](LICENSE)

A simple client-side mod for Minecraft that adds automation for cycling villager trades, specifically targeting Librarian enchanted book trades. It supports both [Easy Villagers](https://www.curseforge.com/minecraft/mc-mods/easy-villagers) and [Trade Cycling](https://www.curseforge.com/minecraft/mc-mods/trade-cycling) mods.

Set your desired enchantment and price, then press a keybind to automatically re-roll trades until you find what you're looking for!

## Features

*   **Dual-Mod Support:** Works with either [Easy Villagers](https://www.curseforge.com/minecraft/mc-mods/easy-villagers) or [Trade Cycling](https://www.curseforge.com/minecraft/mc-mods/trade-cycling) mod - only one is required.
*   **NeoForge Support:** Built for Minecraft NeoForge 1.21.1.
*   **Automated Trade Cycling:** Automatically cycles villager trades repeatedly.
*   **Targeted Search:** Configure the specific enchanted book (enchantment, level, max emerald cost) you want.
*   **Auto-Stop:** Cycling automatically stops when the desired trade is found.
*   **Manual Toggle:** Start and stop cycling easily with a configurable keybind (Default: `R`).
*   **Lightweight & Client-Side:** Doesn't affect server performance or require installation on the server.

## Requirements

**Version:**
*   **NeoForge:** 1.21.1

**Mod Compatibility (only ONE is required):**
*   **[Easy Villagers Mod](https://www.curseforge.com/minecraft/mc-mods/easy-villagers)**
*   **[Trade Cycling Mod](https://www.curseforge.com/minecraft/mc-mods/trade-cycling)**


## How to Use:

- Open Config: Press the config keybind (Default: C) or click the "Cog" button added to the Villager Trade Screen.
- Set Target: In the GUI:
  - Type the Enchantment ID (e.g., minecraft:mending). Suggestions will appear as you type (Tab accepts).
  - Enter the desired Level.
  - Enter the Max Emeralds you'll pay.
  - Use the Cycle Delay button to choose speed (1-5 ticks, lower is faster).
  - Click Save.
- Start/Stop Cycling:
  - Open a Librarian's trade GUI (the mod will work with either Easy Villagers or Trade Cycling interfaces)
  - Press the Toggle Auto Trade Cycling keybind (Default: R) OR click the "Play" button added to the Villager Trade Screen.
  - Press the keybind or click the button again to stop manually.

## Dependencies

This mod **REQUIRES** either the [Easy Villagers Mod](https://www.curseforge.com/minecraft/mc-mods/easy-villagers) OR the [Trade Cycling Mod](https://www.curseforge.com/minecraft/mc-mods/trade-cycling) to be installed. The mod automatically detects which one is present and adapts accordingly.

## Compatibility Details

- Full support for both Easy Villagers and Trade Cycling mods
- Automatically detects which mod is installed at runtime
- Works seamlessly with either mod's interface
- Uses reflection to interact with either dependency without requiring hard dependencies

This flexible approach makes installation much simpler and allows the mod to work with different modpack setups.
