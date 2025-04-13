# Easy Auto Cycler
[![CurseForge Downloads](https://cf.way2muchnoise.eu/full_1242344_downloads.svg?badge_style=flat)](https://curseforge.com/minecraft/mc-mods/easy-auto-cycler)
[![GitHub license](https://img.shields.io/github/license/Uncraftbar/Easy-Auto-Cycler-1.20.1-Forge)](LICENSE)

A simple client-side mod for Minecraft NeoForge that adds automation to the 'Cycle Trades' button from the [Easy Villagers](https://www.curseforge.com/minecraft/mc-mods/easy-villagers) mod, specifically targeting Librarian enchanted book trades.

Set your desired enchantment and price, then press a keybind to automatically re-roll trades until you find what you're looking for!

## Features

*   **Automated Trade Cycling:** Automatically clicks the Easy Villagers 'Cycle Trades' button repeatedly.
*   **Targeted Search:** Configure the specific enchanted book (enchantment, level, max emerald cost) you want using in-game commands.
*   **Auto-Stop:** Cycling automatically stops when the desired trade is found.
*   **Manual Toggle:** Start and stop cycling easily with a configurable keybind (Default: `R`).
*   **Lightweight & Client-Side:** Doesn't affect server performance or require installation on the server.

## Requirements

*   **Minecraft Forge:** 1.20.1
*   **[Easy Villagers Mod](https://www.curseforge.com/minecraft/mc-mods/easy-villagers):** Version 1.1.27 or later for MC 1.20.1


## Usage

1.  **Configure Target Trade:**
    *   Use the `/autocycle setbook <enchantment_id> <level> <max_emeralds>` command.
    *   `<enchantment_id>`: The resource location ID (e.g., `mending`, `minecraft:unbreaking`, `mod_id:enchant_id`).
    *   `<level>`: The desired enchantment level (e.g., `1` for Mending, `4` for Protection IV).
    *   `<max_emeralds>`: The maximum number of emeralds you are willing to pay (1-64).
    *   **Example:** `/autocycle setbook minecraft:mending 1 20` (Find Mending for 20 emeralds or less)
2.  **Check Status:** Use `/autocycle status` to see the currently configured target.
3.  **Clear Target:** Use `/autocycle clear` to remove the target.
4.  **Start/Stop Cycling:**
    *   Open the trade GUI of a Librarian villager that is eligible for trade cycling (the Easy Villagers cycle button must be active).
    *   Press the **Toggle Auto Trade Cycling** keybind (Default: `R`). Check Minecraft controls options to view or change the keybind under the "Easy Auto Cycler" category.
    *   Press the keybind again to stop cycling manually.

## Dependencies

This mod **REQUIRES** the [Easy Villagers Mod](https://www.curseforge.com/minecraft/mc-mods/easy-villagers) to be installed, as it directly interacts with the button added by that mod.
