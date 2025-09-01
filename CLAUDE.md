# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Easy Auto Cycler is a client-side NeoForge mod for Minecraft 1.21.1 that automates villager trade cycling. It provides compatibility with both Easy Villagers and Trade Cycling mods using runtime reflection to avoid hard dependencies.

## Build System & Commands

This project uses Gradle with NeoGradle for Minecraft mod development:

### Essential Commands
- `./gradlew build` - Build the complete mod JAR
- `./gradlew runClient` - Launch Minecraft client with the mod for testing
- `./gradlew runServer` - Launch dedicated server for testing
- `./gradlew clean` - Clean build directory
- `./gradlew jar` - Build mod JAR only

### Development Commands
- `./gradlew runData` - Run data generators
- `./gradlew test` - Run tests
- `./gradlew idea` - Generate IntelliJ IDEA project files

## Core Architecture

### Key Components

**Main Entry Point:**
- `EasyAutoCyclerMod.java` - Mod initialization, event bus registration

**Core Logic:**
- `AutomationManager.java` - Central automation logic with dual mod compatibility
  - Uses reflection to interface with Easy Villagers or Trade Cycling mods
  - Manages filter system for trade matching
  - Handles cycling automation with safety limits

**User Interface:**
- `ClientEventHandler.java` - Client-side event handling
- `InputHandler.java` - Keybinding management
- `gui/` package - Configuration screens and UI components
  - `ConfigScreen.java` - Main configuration interface
  - `FilterEditorScreen.java` - Filter editing interface
  - `FilterListScreen.java` - Filter management

**Configuration System:**
- `filter/FilterEntry.java` - Individual trade filter definitions
- Multi-filter system with AND/OR logic support
- Legacy single-target system for backward compatibility

### Dual Mod Compatibility

The mod uses reflection-based handlers to support both Easy Villagers and Trade Cycling mods without requiring either as a hard dependency:

- `EasyVillagersHandler` - Interfaces with Easy Villagers mod classes
- `TradeCyclingHandler` - Interfaces with Trade Cycling mod classes
- Runtime detection determines which mod is present
- Graceful fallback when neither mod is available

### Filter System

The core filtering logic supports:
- Enchantment-based filters (books and enchanted items)
- Item-based filters with count requirements
- Price limits (emerald cost)
- Multiple filters with AND/OR logic
- Real-time trade evaluation during cycling

## Development Notes

### Mod Dependencies
- Compile-time: Easy Villagers and Trade Cycling as `compileOnly`
- Runtime: Automatic detection using `ModList.get().isLoaded()`
- No hard dependencies required for users

### Key Configuration
- `gradle.properties` - Mod version, Minecraft version, NeoForge version
- `src/main/resources/META-INF/neoforge.mods.toml` - Mod metadata

### Safety Features
- Maximum cycle limit (3000) to prevent infinite loops
- Screen validation before starting automation
- Graceful error handling with user feedback

## Package Structure

```
src/main/java/com/uncraftbar/easyautocycler/
├── EasyAutoCyclerMod.java          # Main mod class
├── AutomationManager.java          # Core automation logic
├── ClientEventHandler.java         # Client events
├── InputHandler.java               # Input handling
├── Keybindings.java                # Key mappings
├── filter/
│   └── FilterEntry.java            # Trade filter definitions
└── gui/
    ├── ConfigScreen.java           # Main config UI
    ├── FilterEditorScreen.java     # Filter editing UI
    ├── FilterListScreen.java       # Filter management UI
    ├── CustomImageButton.java      # Custom UI components
    ├── ScrollableContainer.java    # Scrollable UI container
    └── SuggestingEditBox.java      # Auto-complete text input
```