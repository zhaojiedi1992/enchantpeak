# Changelog

## Unreleased

## 1.1.0

Released on TBD.

### E2E Testing Framework

This release introduces a comprehensive end-to-end testing framework that validates the entire mod pipeline from in-game initialization through JEI/REI plugin registration to actual search functionality — ensuring players can reliably find and use enchantment builds.

- Added: Complete e2e test harness with HeadlessMc integration — real Minecraft client boots, loads into a world, and runs API assertions before auto-exiting
- Added: JEI multi-item validation across 8 representative items (tools/weapons/armor) — verifies that every category has queryable recipes
- Added: REI search validation for 4 key enchantments (fortune/efficiency/sharpness/protection) — simulates player search experience by matching tooltip text
- Added: In-game API assertions with watchdog mechanism (180s timeout, 2s settle after world load)
- Added: Smoke test CI matrix covering Fabric 26.2/1.21.9/1.21.4/1.20.4 and NeoForge 26.2/1.21.4/1.20.4
- Added: `scripts/assert_e2e_log.py` — robust log assertion with file-based pattern input (avoids shell escaping issues)
- Fixed: `push.sh` network probing now runs both before build and before push — the v1.0.19 release failed to push because the initial probe result expired after an hour-long build
- Docs: Added comprehensive `docs/E2E_TEST_COVERAGE.md` documenting the test architecture, coverage scope, and local testing workflow

### Technical Details

The e2e harness (`src/e2e/`) is a separate source set built only with `-Pe2e` and never shipped in release jars. It hooks into Fabric's client entrypoint and JEI/REI plugin registration, waits for world load, then executes assertions on the main thread. Results are printed as machine-readable log lines (`[EnchantPeak E2E] RESULT: OK <details>`) that CI validates via regex.

Only Fabric 26.2 runs the full API assertion harness; other versions perform log-marker checks (mod initialized, recipes registered, no error patterns). This hybrid approach gives high confidence on the latest version while ensuring older versions don't regress.

## 1.0.19

Released on 2026-08-16.

Fault tolerance and diagnosability round.

- Added: enchantment resolution is now tolerant of datapack-removed vanilla
  enchantments - a missing enchantment skips only the builds containing it
  (precise warning in the log) instead of disabling the whole mod; applies to
  all 1.21+/26.x families on both JEI and REI
- Added: JEI/REI registration failures now log actionable messages (enter a
  world to auto-recover; Missing key = an enchantment was removed by a datapack)
- Fixed: inExpectedScope regression from 1.0.18 - old-family deep tests would
  silently skip instead of hard-failing on bootstrap errors (path-segment
  matching now also keeps the 1.20.6 skip intact)
- Chore: unified line endings to LF via .gitattributes; closed-loop verifier
  and structure test parse the new tolerant group form



## 1.0.18

Released on 2026-08-16.

### Highlights

Closed-loop verification for all version families + data fixes it uncovered.

- Added: the Python datapack verifier now parses the actual Java EnchantmentData
  source (helper methods, parameterized factories, all registration forms) and
  cross-checks every build's content against the spec — changing one number in
  the Java data now fails CI. This closed the last verification gap and
  immediately caught real bugs in the 1.21 families: shovels were missing the
  Fortune build, hoes were missing both Fortune and Silk Touch builds, and mace
  builds were 7 jumbled groups instead of 4 correct maximal ones
- Added: 1.20.5/1.20.6 (the only family with no official data source) now has a
  reviewed static snapshot checked against the Java data; the exception is
  documented in the README
- Fixed: REI search entries no longer silently vanish on the title screen —
  registry access is guarded before use and re-registered when entering a world
- Fixed: REI no longer registers duplicate vanilla entries for curse-only items
- Fixed: JEI tooltip arrows no longer hardcode gold color (matches REI styling)
- Cleanup: dead code removed from the structure test, fragile path matching
  replaced with an explicit whitelist, commit/push error output preserved



## 1.0.17

Released on 2026-08-16.

### Highlights

Review round 2: test rigor and release-pipeline hardening.

- Fixed: deep verification tests now hard-fail on bootstrap errors for the
  families they cover (1.18.2-1.20.4) instead of silently skipping — a green
  CI run now actually means the data was verified
- Added: item completeness check — every vanilla item that accepts any
  non-curse enchantment must have a record in the mod data (this is the check
  that would have caught the missing flint_and_steel / curse-only items)
- Fixed: release workflow now runs the Fabric/NeoForge family sync check,
  so manual tag pushes can't ship drifted family code
- Fixed: push.sh push-failure detection was dead code under set -e; tag
  push failures abort loudly again



## 1.0.16

Released on 2026-08-16.

### Highlights

Code-review fixes + data completeness for all legacy versions.

- Fixed: enchantment tooltips now show 'Efficiency V' not 'EfficiencyV' (missing
  space regression in v1.0.15 affected all 1.21+ and 26.x builds, both loaders)
- Fixed: push.sh tag push failures were silently ignored (grep pipe inverted the
  exit code); now aborts loudly with recovery hint
- Fixed: push.sh build failure after version bump left dirty working tree; now
  auto-reverts gradle.properties and CHANGELOG.md
- Fixed: old families (1.18.2-1.20.6) were missing flint_and_steel and 9 curse-only
  items (carved pumpkin, compass, all 7 head items); they are now registered and
  verified by the JVM deep test in all four old families
- Fixed: axe data lines in all 13 family copies had 40-space indentation instead
  of 20-space (cosmetic, no functional impact)

- Fixed: release metadata now uses the correct Java version per target (was hardcoded
  Java 25/Java 21 for all Fabric/NeoForge uploads — Java 17/21 players couldn't see
  the 1.18.2-1.21.x files on Modrinth)
- Fixed: fabric-api is no longer a required dependency (the mod never used it;
  it is now a suggestion, matching REI/JEI)
- Fixed: legacy family enchantment data now matches vanilla semantics exactly —
  deep JVM verification (max level, applicability, exclusivity, maximality,
  complete enumeration of maximal compatible sets) found and fixed incorrect
  axe/sword/armor/shovel/hoe builds in 1.18.2-1.21.8, plus 10 missing items
  (flint_and_steel + curse-only entries) in the 1.21 families
- Added: family source sync guard (scripts/sync_family_sources.py, wired into CI
  and push.sh) — the 13 shared family directories between Fabric and NeoForge can
  no longer silently drift
- Added: datapack deep verification now covers the 1.21 families too (was 1.21.9+/26.x
  only); 1.20.5/1.20.6 remain compile-only (no enchantment data source exists)
- Fixed: enchantment tooltips now use the vanilla enchantment.level.N translation
  key (styles and RTL word order preserved; was a %s %s string format hack)
- Fixed: NeoForge builds are marked client-only via @Mod(dist = Dist.CLIENT)
  (dedicated servers no longer load the mod; 1.20.4's older FML doesn't support
  the attribute and keeps a plain @Mod)
- Changed: push.sh bumps the version before building so dist/ artifacts carry the
  release version; tag push failures now abort loudly instead of silently skipping
  the release
- Cleanup: removed dead EnchantEntry.levelString(), stale forge maven entry,
  empty emi directories


## 1.0.15

Released on 2026-08-15.

### Highlights

Open-source readiness release: jar metadata fixes, full test suite, and parallel builds.

- **Fixed**: Fabric jars now load on every advertised version of a cluster
  (minecraft dependency lists the whole cluster instead of pinning the
  representative build version); mod version is no longer "unspecified";
  JEI suggestion no longer shows a raw ${...} placeholder
- **Fixed**: NeoForge per-cluster dependency ranges in neoforge.mods.toml
  (was a blanket [1.20.3,)) and bundled MIT LICENSE notice
- **Fixed**: legacy families (1.18.2-1.21.8) showed raw translation keys /
  Chinese build names — group names now use shared English keys in all locales
- **Added**: JVM test suite + end-to-end jar metadata verification
  (scripts/verify_jars.py), both wired into CI
- **Perf**: local all-targets build ~5m40s → ~1m15s (Fabric/NeoForge
  pipelines run in parallel, daemon reuse)

- Fixed: legacy version families (1.18.2-1.21.8) showed raw translation keys / Chinese
  build names in-game — build group names now use the same English keys as 1.21.9+/26.x
  across all 7 locales
- Added: JVM test suite (record invariants, source-level data consistency across all
  version families, runtime registry assembly for 1.18-1.21.8), run per target in CI
- Added: `scripts/verify_jars.py` end-to-end jar metadata verification, wired into
  `scripts/build_all_versions.sh` and the CI build/release workflows
- Docs: added CONTRIBUTING.md and issue templates
- CI: workflows now run on main pushes and PRs only; Aliyun mirror is opt-in via
  `-Puse_aliyun_mirror=true`; NeoForge JEI 26.1 baseline aligned with Fabric
- Changelog: 1.0.9-1.0.14 backfilled from git history; `push.sh` accepts `-m` notes
  and CI extracts only the released version's section for platform changelogs
- Fabric jar metadata: `version` no longer builds as `unspecified` (mod version + MC cluster suffix, e.g. `1.0.14+mc1.19.2-1.19.4`)
- Fabric `minecraft` dependency now lists every version of the cluster (e.g. `["1.19.2","1.19.3","1.19.4"]`) instead of pinning the representative build version — cluster jars now load on all advertised versions
- Fabric `suggests.jei` no longer embeds the unexpanded `${jei_min_version}` placeholder
- NeoForge `neoforge.mods.toml` is now generated per cluster: `minecraft` range matches the cluster (e.g. `[1.21,1.21.2)` instead of a blanket `[1.20.3,)`), and mod/JEI/NeoForge version bounds come from `neoforge/targets.json`
- NeoForge jars now bundle the MIT `LICENSE` notice
- Added `mc_range` to `neoforge/targets.json` (per-cluster Minecraft version range used for the mods.toml dependency)


## 1.0.14

Released on 2026-08-15.

### Added
- NeoForge support: 8 clusters (1.20.3–26.2) alongside Fabric, with JEI integration
- Local build script now builds all NeoForge targets too

### Fixed
- CI: JDK setup order for NeoForge jobs (21 must be the default `JAVA_HOME`; Gradle 8.14 daemon cannot run on Java 25)
- NeoForge mod version now follows the root `gradle.properties` (single source of truth)
- CI: GitHub release now downloads both Fabric and NeoForge artifacts

## 1.0.13

Released on 2026-08-15.

### Changed
- Jar naming now shows the version range (e.g. `mc1.19.2-1.19.4`) for multi-version targets

## 1.0.12

Released on 2026-08-15.

### Fixed
- Corrected 26.x `game_versions` for Modrinth/CurseForge API (`26.1`, not `1.26.1`)

## 1.0.11

Released on 2026-08-15.

### Fixed
- `game-versions` multi-line output for mc-publish (single-line string was rejected by the Modrinth API)

## 1.0.10

Released on 2026-08-15.

### Added
- Full MC 1.18.2–26.x coverage via version-cluster targets with community jar naming (`fabric-<version>+mc<range>`)

## 1.0.9

Released on 2026-08-14.

### Added
- Multi-version support for MC 1.18.2–26.x with per-target CI verification
- Verified version matrix (`versions/minecraft.json`) and per-target dependencies

### Fixed
- Per-version API corrections for all MC families (1.18.2–26.2)
- Java 21 as runner JDK for fabric-loom 1.17+ compatibility

## 1.0.8

Released on 2026-08-14.

### Fixed
- Removed `registerExtraIngredients` to prevent replacing vanilla items in JEI
- Hardened the release script against dirty worktrees and unverified builds
- Made enchantment verification portable and enforced it in CI

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
