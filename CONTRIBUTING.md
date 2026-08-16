# Contributing to EnchantPeak

Thanks for your interest in contributing! 🎉

## Reporting issues

- Include your Minecraft version, loader (Fabric/NeoForge), and JEI/REI versions
- For enchantment data problems (wrong level cap, missing build, conflict rule), the
  mod verifies everything against the official vanilla datapack — tell us which item
  and build look wrong so we can re-verify

## Development setup

**Requirements:** JDK 25 (for MC 26.x targets), JDK 21 (MC 1.21.x / NeoForge), JDK 17
(MC 1.18.2–1.20.1). Gradle toolchains auto-select the right JDK per target.

```bash
git clone https://github.com/zhaojiedi1992/enchantpeak.git
cd enchantpeak
./gradlew build                                  # Fabric, default target 26.2
./gradlew build -Ptarget_mc=1.21                 # a specific Fabric cluster
scripts/build_all_versions.sh                    # all Fabric + NeoForge clusters
```

Behind a slow connection to Maven Central? Prepend `-Puse_aliyun_mirror=true`.

NeoForge lives in `neoforge/` as an independent Gradle build:

```bash
cd neoforge && ./gradlew build -Ptarget_mc=1.21.8
```

Version clusters and dependency versions are defined once in
`versions/minecraft.json` (Fabric) and `neoforge/targets.json` (NeoForge) — don't
hardcode versions elsewhere. Build metadata (fabric.mod.json / neoforge.mods.toml)
is generated per cluster at build time.

## Project layout

- `src/main/` — loader-agnostic shared code (common data records) and resources
- `src/<family>/` — per Minecraft-API-era code: `EnchantmentData` (per-version
  enchantment tables), JEI/REI plugins, `EnchantStacks`
- `neoforge/src/` — mirrors the same family structure for NeoForge
- `scripts/verify_enchants*.py` — cross-checks the shipped enchantment data against
  the vanilla datapack (runs in CI for every target)

## Pull requests

- Keep PRs focused; one logical change per PR
- Run `scripts/build_all_versions.sh` (or at least the clusters you touched) before
  submitting — CI runs the same builds
- New user-facing strings go into `src/main/resources/assets/enchantpeak/lang/en_us.json`
  (other locales welcome; English must always be complete)
- Conventional commits appreciated (`fix:`, `feat:`, `docs:`, …)

## End-to-end testing

Three tiers, deepest first:

1. **Data closed-loop (all targets, every CI run)** — JVM deep tests, the Python
   datapack verifier (`scripts/verify_enchants.py`), and jar metadata checks.
2. **In-game API assertions (26.x targets)** — `src/e2e/` harness, built only with
   `-Pe2e`: `./gradlew e2eJar -Ptarget_mc=26.2 -Pe2e`. Stage it alongside the mod:
   `./gradlew stageE2eMods -Ptarget_mc=26.2` (copies the jar + JEI/REI/fabric-api
   into `run/mods`). After entering a world the harness prints
   `[EnchantPeak E2E] RESULT: OK <details>` and quits (exit 0/1).
3. **Headless client smoke (representative matrix, tag pushes /
   `gh workflow run e2e-smoke.yml`)** — boots the real client via mc-runtime-test
   (Xvfb + auto world join) for fabric 26.2/1.21.9/1.21.4/1.20.4/1.18.2 and
   neoforge 26.2/1.21.9, then asserts the log markers with
   `scripts/assert_e2e_log.py`.

Local quick check after manual launches: point your launcher at `run/` and run
`python3 scripts/assert_e2e_log.py --require 'JEI recipes registered: \d+' ...`.

## Releasing (maintainers)

`./push.sh -m "release notes"` bumps the patch version, converts the CHANGELOG
"Unreleased" section into the new version, tags, and pushes — CI builds and
publishes to Modrinth/CurseForge/GitHub Releases from the tag.
