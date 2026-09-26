# Automated publishing

The `Publish release` GitHub Actions workflow builds only the selected branches,
publishes their jars to Modrinth and CurseForge, and creates a GitHub Release with
the same version and changelog. Branches follow `<loader>-<minecraft>` and jars
follow `easyautocycler-<loader>-<minecraft>-<mod-version>.jar`.

`targets.json` is the shared source for both the workflow matrix and publisher:

- `release-3.1.2` (default): eight new/changed targets, Fabric + NeoForge for
  1.21.11, 26.1.2, 26.2, and 26.3.
- `keybinding-fixes`: the six older targets changed by PRs #34–39.
- `26.3`: the two new ports.
- Individual branch names: single-target publication/recovery.
- `all`: all twelve supported targets; use only for an intentional full release.

The 1.20.1 and 1.21.1 branches are deliberately excluded from 3.1.2. Their current
tips match the exact source commits built in their latest successful releases
(3.1.1 for Fabric 1.20.1; 3.1.0 for the other three).

## Required repository secrets

- `MODRINTH_TOKEN`: a Modrinth personal access token with Create versions, Read versions, and Write versions scopes.
- `CURSEFORGE_TOKEN`: a CurseForge API token.

## Release process

1. Compare branches with the source commits used by the last successful publish,
   not just the shared GitHub tag (which points to `main`). Select only changed
   targets and prepare their release versions. Push those branches and `main`.
2. Open **Actions → Publish release → Run workflow**.
3. Select the version and target set. Leave `publish` disabled for the first run.
   The workflow resolves branch tips to immutable commits, checks every version,
   builds/tests the selected jars, checks their count, and dry-runs the publisher.
4. Inspect the successful dry run and its artifacts.
5. Run the same version and target set with `publish` enabled to upload the
   selected releases and create the matching GitHub Release. Do not move branch
   tips between dry-run validation and publication.

The `destination` input normally stays set to `all`. After a partial failure,
inspect which files actually uploaded, then select only the missing branch and
platform (`modrinth` or `curseforge`). Do not blindly repeat successful uploads.
Any real publish also creates or updates the GitHub Release. Configuration and
changelog are read from the workflow's immutable commit rather than moving `main`.

The publisher defaults to dry-run mode even when invoked locally:

```text
./release/gradlew -p release publishMods -PreleaseVersion=3.1.2 -PreleaseTarget=release-3.1.2
```

Actual local publication requires both token environment variables and an explicit opt-out from dry-run mode:

```text
./release/gradlew -p release publishMods -PreleaseVersion=3.1.2 -PreleaseTarget=release-3.1.2 -PdryRun=false
```
