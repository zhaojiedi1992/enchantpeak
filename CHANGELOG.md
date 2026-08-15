# Changelog

## 1.0.14

Released on 2026-08-15.


## 1.0.13

Released on 2026-08-15.


## 1.0.12

Released on 2026-08-15.


## 1.0.11

Released on 2026-08-15.


## 1.0.10

Released on 2026-08-15.


## 1.0.9

Released on 2026-08-14.


## 1.0.8

Released on 2026-08-14.


## 1.0.7

Released on 2026-08-14.

### Fixed
- Removed `registerExtraIngredients` to prevent replacing vanilla items in JEI
- Hardened the release script against dirty worktrees and unverified builds
- Made enchantment verification portable and enforced it in CI
- Covered all 92 vanilla enchantable items, including explicit empty recommendations for 9 curse-only items

### Changed
- Added independently compiled and verified builds for Minecraft 26.1, 26.1.1, 26.1.2, and 26.2
- Unified local validation, CI matrices, JAR naming, and platform publishing through one version manifest
- Clarified the feature differences between JEI and REI
- Expanded the verified build set from 90 to 288 maximal compatible combinations
- Aligned JEI and REI display names, localized enchantment details, and information pages
- Removed unsupported pinyin-search claims; REI standalone entries remain a platform-specific enhancement

## 1.0.6

Released on 2026-08-13.

### Added
- Shears coverage (Efficiency V + Unbreaking III + Mending I)

### Fixed
- JEI search issues

### Docs
- Use absolute GitHub raw URLs for screenshots

## 1.0.5

Released on 2026-08-12.

### Added
- Elytra and shield coverage (Unbreaking III + Mending I)

### Docs
- Add in-game screenshots to README

## 1.0.4

Released on 2026-08-11.

### Added
- Complete enchantment coverage, verified against the MC 26.2 official datapack

### Fixed
- Review-found gaps in enchantment coverage + JEI layout

### Test
- Add deep verification script (4 checks for modpack-grade accuracy)

### Docs
- Update download links (Modrinth + CurseForge now live)

## 1.0.3

Released on 2026-08-11.

### Changed
- Bilingual README + code review fixes

### Chore
- Add `push.sh` one-click release script

## 1.0.2

### Added
- CurseForge auto-publish in CI workflow
- Quilt loader support (natively compatible with Fabric mod)
- Release metadata initially advertised MC 26.1 through 26.2 compatibility

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
- Localized item and enchantment search through native recipe-viewer tooltips
- Target: Minecraft 26.2, Fabric, Java 25
