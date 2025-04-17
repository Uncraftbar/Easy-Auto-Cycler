# Easy Auto Cycler

[![CurseForge Downloads](https://cf.way2muchnoise.eu/full_1242344_downloads.svg?badge_style=flat)](https://curseforge.com/minecraft/mc-mods/easy-auto-cycler)
[![GitHub license](https://img.shields.io/github/license/Uncraftbar/Easy-Auto-Cycler)](LICENSE)

A simple client-side mod for Minecraft NeoForge that adds automation to the 'Cycle Trades' button from the [Easy Villagers](https://www.curseforge.com/minecraft/mc-mods/easy-villagers) mod, specifically targeting Librarian enchanted book trades.

Set your desired enchantment and price, then press a keybind to automatically re-roll trades until you find what you're looking for!

## Features

*   **Automated Trade Cycling:** Automatically clicks the Easy Villagers 'Cycle Trades' button repeatedly.
*   **Targeted Search:** Configure the specific enchanted book (enchantment, level, max emerald cost) you want using in-game commands.
*   **Auto-Stop:** Cycling automatically stops when the desired trade is found.
*   **Manual Toggle:** Start and stop cycling easily with a configurable keybind (Default: `R`).
*   **Lightweight & Client-Side:** Doesn't affect server performance or require installation on the server.

## Requirements

*   **Minecraft Neoforge:** 1.21.1
*   **[Easy Villagers Mod](https://www.curseforge.com/minecraft/mc-mods/easy-villagers):** for MC 1.21.1


## How to Use:

- Open Config: Press the config keybind (Default: C) or click the "Cog" button added to the Villager Trade Screen.
- Set Target: In the GUI:
  - Type the Enchantment ID (e.g., minecraft:mending). Suggestions will appear as you type (Tab accepts).
  - Enter the desired Level.
  - Enter the Max Emeralds you'll pay.
  - Use the Cycle Delay button to choose speed (1-5 ticks, lower is faster).
  - Click Save.
- Start/Stop Cycling:
  - Open a Librarian's trade GUI (make sure the Easy Villagers cycle button would be active).
  - Press the Toggle Auto Trade Cycling keybind (Default: R) OR click the new "Play" button added to the Villager Trade Screen.
  - Press the keybind or click the button again to stop manually.

## Dependencies

This mod **REQUIRES** the [Easy Villagers Mod](https://www.curseforge.com/minecraft/mc-mods/easy-villagers) to be installed, as it directly interacts with the button added by that mod.
