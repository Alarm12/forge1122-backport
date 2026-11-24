# Forge 1.12.2 build attempt (2026-11-24)

Target: Java 8 / Forge 14.23.5.2860 / ForgeGradle 3.x, both standalone mod JARs.

## Attempt results

- Loaded both modules and confirmed the full source archive is present.
- `gradle clean build --stacktrace`: not started — Gradle executable is not installed (`gradle: command not found`).
- No Gradle wrapper was supplied with the original source archive.
- Available JDK: OpenJDK 21; no JDK 8 installed.
- `curl -I https://maven.minecraftforge.net/`: blocked (`Could not resolve host`).
- Downloading the Forge 1.12.2 MDK through the available download facility also failed.
- **Therefore ForgeGradle `compileJava`, `reobfJar`, and actual mod JAR generation did not run and are not claimed to have passed.**

## Source-level changes made in this attempt

- Removed deprecated JCenter dependency repository.
- Moved client packet state changes from the Netty thread to the client thread via `FMLCommonHandler#getWorldThread`.
- Rejected malformed attachment message references and validated cable targeting again on the server.
- Reset client attachment state on disconnect.
- Blocked movement into unloaded chunks and solid collision boxes.
- Added an optional GitHub Actions workflow for a cloud build, suitable for running from a mobile browser.

## Cloud build

`.github/workflows/build.yml` configures Java 8 + Gradle 4.10.3, executes `gradle clean build --stacktrace`, and uploads build artifacts/logs. It has not been executed here.

## Local build

Install JDK 8 and Gradle 4.10.3, then run:

```sh
gradle clean build --stacktrace
```

If successful, compiled/reobfuscated JARs are generated under both modules' `build/libs/` directories.

## Offline Java core smoke-test

Command: `python tools/offline_core_test.py`

Result:

```text
PASS: Java 8 bytecode, chain math, NBT, storage, provider lookup, zipline state
Compiled 13 real Java files against offline test doubles.
Note: full ForgeGradle compilation was not possible without Forge libraries.
```

This test checks Java 8 syntax and some core behavior, but its test doubles
cannot verify every 1.12.2 Forge/Minecraft API signature.
