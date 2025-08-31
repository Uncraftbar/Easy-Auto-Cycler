# Easy Auto Cycler - Changelog

## v2.0.0 - Advanced Filtering System

### 🎉 Major Features
- **Complete UI Redesign:** Unified configuration interface combining general settings and advanced filtering
- **Advanced Filter System:** Create multiple complex filter rules with various criteria:
  - Target specific enchantments with minimum level requirements
  - Filter by item type with minimum quantity requirements
  - Support for custom payment items (not just emeralds)
  - Flexible AND/OR logic for multiple filters
- **Session Persistence:** Filters automatically save to `config/easyautocycler-filters.json` and restore between game sessions

### ✨ Enhancements
- **Improved Autocomplete:** Enhanced suggestions for enchantment and item IDs with better UI
- **Minimum Level Matching:** Enchantment level filtering now uses minimum requirements instead of exact matching
- **Custom Payment Support:** Trades can now require payment in any item, not just emeralds
- **Better User Experience:** 
  - Intuitive filter editor with field validation
  - Enable/disable filters without deleting them
  - Clear error messages and help text
  - Improved button labeling and layout

### 🔧 Technical Improvements
- **Memory Management:** Fixed UI state persistence when canceling edits
- **Code Architecture:** Cleaner separation between UI and logic components
- **Configuration System:** Robust JSON-based configuration with error handling
- **Registry Integration:** Better integration with Minecraft's item and enchantment registries

### 🐛 Bug Fixes
- Fixed text blur issues in filter screens
- Fixed widget positioning and scrolling in filter lists  
- Resolved filter state inconsistencies when canceling edits
- Improved character limits for enchantment/item ID inputs

### 🔄 Migration
- Legacy single-target configurations are automatically migrated to the new filter system
- Old configuration values are preserved during the transition
- No manual intervention required for existing users

---

**For Modrinth/CurseForge Description:**

# v2.0.0 - Advanced Filtering System 🎉

This major update completely transforms Easy Auto Cycler with a powerful new filtering system!

## What's New:
- **Multiple Filter Rules:** Create complex filter combinations instead of single targets
- **Custom Payment Items:** Support for any payment item, not just emeralds  
- **Session Persistence:** Your filters save automatically and restore between sessions
- **Enhanced UI:** Redesigned interface with better autocomplete and validation
- **Minimum Level Matching:** Enchantment levels now work as minimums (level 3+ matches 3, 4, 5...)
- **Flexible Logic:** Choose AND/OR logic for multiple filter combinations

## Breaking Changes:
- Old single-target configuration is automatically migrated to new filter system
- UI has been completely redesigned (much better!)

Perfect for power users who want precise control over their villager trade automation!

**Requirements:** NeoForge 1.21.1 + Either Easy Villagers OR Trade Cycling mod