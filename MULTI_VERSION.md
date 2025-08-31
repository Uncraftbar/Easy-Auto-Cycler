# Multi-Version Repository Structure

This repository uses a **branch-based approach** for supporting multiple Minecraft versions and mod loaders.

## Branch Structure

| Branch | Minecraft Version | Mod Loader | Features | Status |
|--------|------------------|------------|----------|--------|
| `main` | 1.21.1 | NeoForge | Advanced Filtering System | ✅ Active |
| `neoforge-1.21.1` | 1.21.1 | NeoForge | Advanced Filtering System | ✅ Active |
| `forge-1.20.1` | 1.20.1 | Forge | Basic Enchantment Targeting | ✅ Maintenance |

## Development Workflow

### For Contributors:
1. **Feature Development**: Create feature branches from the target version branch
2. **Bug Fixes**: Apply to the appropriate version branch(es)
3. **Cross-Version Features**: Start with latest version, then backport if needed

### For Users:
1. **Latest Features**: Use `main` branch (currently NeoForge 1.21.1)
2. **Specific Version**: Switch to the appropriate version branch
3. **Releases**: Download from [Releases](https://github.com/Uncraftbar/Easy-Auto-Cycler/releases) page

## Release Strategy

- **Version Tags**: Format `v{minecraft-version}-{mod-version}` (e.g., `v1.21.1-2.0.0`)
- **Branch Releases**: Each branch maintains independent version numbers
- **Changelog**: Maintained per version in branch-specific changelogs

## Migration from Separate Repository

The 1.20.1 Forge version was migrated from `Easy-Auto-Cycler-1.20.1-Forge` into this repository as the `forge-1.20.1` branch to centralize development and maintenance.

## Mod Compatibility by Version

### NeoForge 1.21.1 (`main`, `neoforge-1.21.1`)
- ✅ Easy Villagers
- ✅ Trade Cycling
- ✅ Advanced Filtering System
- ✅ Session Persistence
- ✅ Custom Payment Items

### Forge 1.20.1 (`forge-1.20.1`)
- ✅ Easy Villagers
- ❌ Trade Cycling (not supported)
- ❌ Advanced Filtering (basic targeting only)
- ❌ Session Persistence
- ❌ Custom Payment Items

---

This structure ensures easy maintenance while providing clear separation between different Minecraft versions and feature sets.