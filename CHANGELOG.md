# Changelog

## 1.0.6

Released on 2026-08-13.


## 1.0.5

Released on 2026-08-12.


## 1.0.4

Released on 2026-08-11.


## 1.0.3

Released on 2026-08-11.


## 1.0.2

### Added
- CurseForge auto-publish in CI workflow
- Quilt loader support (natively compatible with Fabric mod)
- Multi-version support: MC 26.1, 26.1.1, 26.1.2, 26.2

### Fixed
- JEI plugin now properly loads via `jei_mod_plugin` entrypoint
- REI `registerEntries` error handling catches `Throwable` instead of `Exception`
- Removed duplicate `getAllRecords()` calls in JEI plugin
- Updated to non-deprecated `IRecipeType` API
- Corrected Gradle dependency scope: `compileOnly` instead of `compileOnlyApi`
- Tightened version constraints for JEI (>=30.18.0) and REI (>=26.2) in fabric.mod.json

## 1.0.0

Initial release.

- REI plugin: searchable enchantment build entries + Information tab display
- JEI plugin: custom category with per-item enchantment tooltips
- Covers diamond/netherite tools, weapons, armor, and fishing rod (22 items, 36 builds)
- Chinese, pinyin, and English search support
- Target: Minecraft 26.2, Fabric, Java 25

