# Copilot Instructions — Easy Auto Cycler

## Project Overview

Minecraft Forge mod (1.20.1) that automates trade cycling with support for both [Easy Villagers](https://www.curseforge.com/minecraft/mc-mods/easy-villagers) and [Trade Cycling](https://www.curseforge.com/minecraft/mc-mods/trade-cycling) mods. Client-side only. At least one of the two trading mods must be installed. Easy Villagers is prioritized when both are present.

**Mod integration**: Uses reflection-based handlers to avoid hard compile-time dependencies. Both trading mods are optional (`mandatory=false` in `mods.toml`). Easy Villagers is still listed as `compileOnly` in `build.gradle` for IDE support, but all calls go through reflection at runtime.

## Build Commands

```sh
# Full build (produces reobfuscated jar in build/libs/)
./gradlew build

# Run Minecraft client with the mod loaded
./gradlew runClient

# Run data generators
./gradlew runData
```

There are no tests (`src/test/` is empty). No linter is configured.

## Architecture

### Mod Detection & Initialization

`AutomationManager.initialize()` is called during `FMLCommonSetupEvent`. It checks `ModList.get().isLoaded()` for `easy_villagers` and `trade_cycling`, then instantiates reflection-based handler inner classes:

- **`EasyVillagersHandler`** — Reflects into `CycleTradesButton.canCycle()`, `Main.SIMPLE_CHANNEL`, and `MessageCycleTrades` to send packets via Easy Villagers' SimpleChannel.
- **`TradeCyclingHandler`** — Reflects into `CycleTradesButton.canCycle()` for the trade check. Sends packets using vanilla `ServerboundCustomPayloadPacket` with `ResourceLocation("trade_cycling", "cycle_trades")` — no reflection needed for sending since Trade Cycling uses an `EventNetworkChannel` with `ACCEPTVANILLA`.

### Core Loop

`EasyAutoCyclerMod` → registers `ClientEventHandler` and `InputHandler` on the Forge event bus during `FMLClientSetupEvent`.

- **`ClientEventHandler`** — Adds config/toggle GUI buttons to `MerchantScreen` via `ScreenEvent.Init.Post`. Drives the automation loop via `TickEvent.ClientTickEvent` → `AutomationManager.clientTick()`.
- **`InputHandler`** — Handles keybind presses (`R` to toggle cycling, `C` to open config).
- **`AutomationManager`** (singleton via `INSTANCE`) — State machine for the cycling loop. Delegates to whichever handler is available via `canCycleTrades()` and `sendCyclePacket()`, checks current offers against filters, and stops when a match is found or the safety limit (3000 cycles) is reached.

### Filter System

`AutomationManager` holds a `List<FilterEntry>` with AND/OR match logic (`matchAny` flag). A legacy single-target system coexists for backward compatibility — `migrateOldConfigToFilters()` bridges the two.

- **`FilterEntry`** — POJO with optional enchantment ID, enchantment level, item ID, min count, payment item ID, and max price. A filter is valid if it has at least an enchantment or item ID.
- **`FilterConfig`** — JSON persistence to `config/easyautocycler-filters.json` using Gson. Converts between `FilterEntry` and `FilterData` (serializable form with string resource locations).

### GUI Layer (`gui/` package)

Screen hierarchy uses Minecraft's `Screen` base class:

- **`ConfigScreen`** — Main config screen with inline filter list, delay control, save/clear. Opened from `MerchantScreen` button or keybind.
- **`FilterListScreen`** — Standalone filter management (alternative entry point, same pattern as `ConfigScreen`'s filter section).
- **`FilterEditorScreen`** — Per-filter editor with validated inputs for enchantment, item, payment, and price. Uses `Consumer<Integer>` callback to return results to the parent screen.
- **`SuggestingEditBox`** — `EditBox` subclass with autocomplete dropdown populated from Minecraft registries.
- **`ScrollableContainer`** — Scrollable widget container for filter lists.
- **`CustomImageButton`** — Image-based button with normal/hover textures loaded from `assets/easyautocycler/gui/`.

## Key Conventions

- **Minecraft 1.20.1 APIs**: Use `EnchantmentHelper.getEnchantments()` (returns `Map<Enchantment, Integer>`), `registryAccess().registryOrThrow(Registries.X)` for registry lookups. Do not use 1.20.2+ holder-based APIs.
- **Resource locations**: Use `ResourceLocation.tryParse()` for static constants, `new ResourceLocation()` or `ResourceLocation.parse()` for user input. The `minecraft:` namespace is implicit when omitted.
- **All UI text** goes through `en_us.json` translation keys using `Component.translatable()`. Keys follow the pattern `gui.easyautocycler.<screen>.<element>`.
- **Keybindings** use Forge's `KeyMapping` with `KeyConflictContext.GUI` (for in-screen binds) or `KeyConflictContext.IN_GAME`.
- **Mod properties** (ID, version, Forge/MC versions) are defined in `gradle.properties` and substituted into `mods.toml` and `build.gradle` via `processResources`.
- **No commands**: The README mentions `/autocycle` commands but these are not implemented in the current codebase. The GUI is the only configuration interface.
- **`Source-Code-Reference/`** contains Easy Villagers and Trade Cycling source for reference when working with their APIs. It is not compiled.
- **Client-side only**: This mod has no server-side code and registers no network channels. It piggybacks on the trading mods' existing network protocols.
