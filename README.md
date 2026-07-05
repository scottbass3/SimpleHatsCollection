# Simple Hats Collection

A multiloader (NeoForge + Fabric) **addon for [SimpleHats](https://github.com/fonnymunkey/SimpleHats)**
on **Minecraft 1.21.1** that gives every player a personal, unbounded **hat collection**.

## Features

- **Deposit from your inventory** — hold **Shift** and **right-click** a hat while hovering it in any
  inventory screen to store it in your collection.
- **Deposit straight from a bag** — **Shift + right-click** a SimpleHats hat bag to roll a random hat
  and send it directly into your collection (a normal right-click still opens the bag as usual).
- **Browse your collection** — press **H** (rebindable) to open the collection screen. It shows one
  slot per hat: unlocked hats display their icon and a stored count, missing hats are greyed out.
- **Withdraw** — click an unlocked hat to take one back into your inventory. Hats you stored in several
  variants or dye colours open a small pick-list so you choose exactly which one to withdraw.

The collection works like an infinite chest: depositing the same hat twice keeps a counter, and you can
withdraw each copy. Variants (`custom_model_data`) and dye colours (`dyed_color`) are preserved. Data is
per-player, persists through death, and is server-authoritative.

## Requirements

- Minecraft **1.21.1**
- **SimpleHats 0.4.0+** and its dependencies (**Accessories**, **oωo lib**)
- NeoForge **21.1.x** *or* Fabric Loader **0.16.5+** with Fabric API

## Project layout

```
common/     shared code (data model, screen, networking payloads, SimpleHats bridge)
neoforge/   NeoForge entrypoint: data attachment, payload handlers, events, keybind
fabric/     Fabric entrypoint: data attachment, payload handlers, events, keybind
buildSrc/   convention plugins for the common/loader split
```

The heavy lifting lives in `common`; each loader only wires platform seams
(`io.github.scottbass3.simplehatscollection.core.Platform`).

## Building

Requires **JDK 21**.

```bash
./gradlew build
```

Loader jars are produced under `neoforge/build/libs/` and `fabric/build/libs/`.

### Dependencies

SimpleHats is not published to Maven Central or Modrinth (the Forge/Fabric mod), so it is pulled from
**CurseMaven** (project `simplehats`, id `631990`). The file ids are set in `gradle.properties`:

```properties
simplehats_curse_project=631990
simplehats_curse_neoforge=5970513   # simplehats-neoforge-1.21.1-0.4.0.jar
simplehats_curse_fabric=5970517     # simplehats-fabric-1.21.1-0.4.0.jar
```

Accessories and oωo lib come from the [Wispforest maven](https://maven.wispforest.io/releases).

## Running in a dev environment

```bash
./gradlew :neoforge:runClient
./gradlew :fabric:runClient
```

Both runs include SimpleHats + Accessories + oωo so the hats exist in-game.

## Releasing

Minecraft versions are managed **by branch** (e.g. `MultiLoader-1.21.1` targets MC 1.21.1, read from
that branch's `gradle.properties`). To publish a release, push a tag `vX.Y.Z` on the branch for the
Minecraft version you are releasing:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The [`Release`](.github/workflows/release.yml) GitHub Actions workflow then builds both loaders and
creates a GitHub release with the jars, named `simplehatscollection-<loader>-<mcversion>-<modversion>.jar`.
A tag containing a hyphen (e.g. `v1.0.0-beta.1`) is published as a pre-release.

## License

Code is licensed under the **MIT License** (see `LICENSE`). This project does not bundle any SimpleHats
assets; those remain the property of their authors.
