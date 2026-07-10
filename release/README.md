# Automated publishing

The `Publish release` GitHub Actions workflow builds all supported branches, publishes their jars to Modrinth and CurseForge, and creates a GitHub Release containing all ten jars with the same version and changelog.

## Required repository secrets

- `MODRINTH_TOKEN`: a Modrinth personal access token with Create versions, Read versions, and Write versions scopes.
- `CURSEFORGE_TOKEN`: a CurseForge API token.

## Release process

1. Push the prepared release commits on `main` and every supported release branch.
2. Open **Actions → Publish release → Run workflow**.
3. Leave `publish` disabled for the first run. The workflow builds all ten jars and performs a Mod Publish Plugin dry run.
4. Inspect the successful dry run and its artifacts.
5. Run the workflow again with `publish` enabled to upload all ten releases and create the matching GitHub Release.

The `destination` input normally stays set to `all`. Select `modrinth` or `curseforge` to safely retry only one platform after a partial platform outage or API failure. Any real publish also creates or updates the GitHub Release, so retries safely restore missing release assets.

The publisher defaults to dry-run mode even when invoked locally:

```text
./release/gradlew -p release publishMods -PreleaseVersion=3.1.0
```

Actual local publication requires both token environment variables and an explicit opt-out from dry-run mode:

```text
./release/gradlew -p release publishMods -PreleaseVersion=3.1.0 -PdryRun=false
```
