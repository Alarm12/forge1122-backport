# Forge 1.12.2 backport workspace

Target:

- Minecraft 1.12.2
- Forge 14.23.5.2860
- Java 8
- `connectiblechains` and `zipline` remain separate mod JARs

Current implementation baseline:

- Reconnectible Chains:
  - chain item
  - fence/wall/iron-bars anchors
  - persistent chain connections via `WorldSavedData`
  - cross-stack chain consumption
  - per-connection slack adjustment with shears
  - shears removal/refund
  - full-dimension synchronization
  - client line rendering
  - public connection API for integrations
- Ziplines:
  - cable/provider API
  - Reconnectible Chains provider
  - right-click attachment detection
  - server-authoritative zipline state
  - arcade and gravity-based movement
  - jump/release detach
  - configurable speed, snap radius, reach, hang offset, cooldown and durability
  - configurable attachment-item registry-name whitelist

Not implemented yet:

- physical chain collision entities
- original knot entity visuals/interaction parity
- chain-link textured mesh rendering
- automatic cable-to-cable transitions at junctions
- integrations besides Reconnectible Chains

Build:

1. Install a Java 8 JDK.
2. Use Gradle 4.10.3 with this workspace.
3. Run `gradle clean build`.
4. Result JARs are written under each module's `build/libs/`.

This workspace intentionally does not bundle a Gradle wrapper JAR.

## Android/iPhone: optional cloud build

This repository includes `.github/workflows/build.yml`. If it is uploaded to a
GitHub repository, the Actions tab can run its build without a local PC:

1. Commit the *contents* of this directory at the repository root (including `.github`).
2. Open **Actions → Build Forge 1.12.2 mods → Run workflow**.
3. If the job succeeds, open the `forge-1.12.2-mods` artifact and download both mod JARs.
4. If the job fails, download the `compile-log` artifact and use its errors to fix the source.

**Important:** the workflow is provided but **not yet tested** against the real
ForgeGradle toolchain. A successful offline smoke test is not proof that the mod loads.
