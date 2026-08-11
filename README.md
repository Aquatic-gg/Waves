# Waves

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/waves/badge)](https://www.codefactor.io/repository/github/mrlarkyy/waves)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/waves?color=40c14a&name=Reposilite)](https://repo.aquatic.gg/#/releases/gg/aquatic/waves)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

Waves is a modular set of Kotlin libraries for Paper/Spigot Minecraft servers. It leans on Kotlin coroutines and
packet-level abstractions so features like fake entities, custom GUIs, and configurable logic can be built without going
through the standard Bukkit API for everything.

## Design notes

- Many visuals (holograms, fake entities) live only in the client via the `Pakket` module, so they do not occupy
  server-side entities.
- Actions, database access, and input handling are built around coroutines and run off the main thread where possible.
- High-read data can be stored in `SnapshotMap`, which is optimized for frequent iteration.

## Modules

Each module handles one part of plugin development and can be depended on independently.

| Module                                     | Purpose                                                                                          |
|:-------------------------------------------|:-------------------------------------------------------------------------------------------------|
| [AquaticCommon](./aquatic-common)          | Shared foundation: `ArgumentContext` for type-safe parsing, coroutine scopes, Bukkit extensions. |
| [Pakket](./pakket)                         | NMS/packet abstraction for client-side entity spawning, metadata, and passenger packets.         |
| [Execute](./execute)                       | Serializable logic engine for defining `Actions` and `Requirements` in configuration.            |
| [KMenu](./kmenu)                           | Packet-based inventory GUI framework with async click handling, pagination, and live updates.    |
| [Kommand](./kommand)                       | Type-safe command routing on top of Brigadier.                                                   |
| [KLocale](./klocale)                       | Per-player localization with Adventure/MiniMessage support.                                      |
| [SnapshotMap](./snapshot-map)              | Read-optimized map wrapper for high-frequency iteration.                                          |
| [KRegistry](./kregistry)                   | Registry system for managing lifecycles and lookups of plugin components.                        |
| [KEvent](./kevent)                         | Coroutine-friendly event bus.                                                                    |
| [Kurrency](./kurrency)                     | Economy abstraction supporting multiple providers (Vault, PlayerPoints, etc.) and custom types.  |
| [Blokk](./blokk)                           | Block abstraction for reading block data and placing blocks across multiple plugins.             |
| [Replace](./replace)                       | String/Component placeholder engine with caching and throttled updates.                          |
| [Stacked](./stacked)                       | ItemStack utility library for item building and click handling with plugin hooks.                |
| [Statistik](./statistik)                   | Player statistic tracking.                                                                        |
| [TreePAPI](./tree-papi)                    | DSL for building PlaceholderAPI expansions.                                                       |

## Feature overview

### Packet-based "fake" objects

Located in `gg.aquatic.waves.clientside`:

- Client-side entities: spawn NPCs, blocks, or models visible only to specific players.
- ModelEngine integration via `FakeMEG` for custom models through packets.
- Packet-level click detection mapped back to Bukkit-like interaction events.

### Scriptable actions (`Execute`)

- Type-safe parameter parsing (Int, String, Collection, etc.) for actions.
- Requirements to gate actions behind checks (permissions, currency, etc.).
- Nested logical conditions defined directly in configuration.

### Economy and data (`Kurrency` and `Statistik`)

- `Kurrency`: a single API for multiple currency types (Vault, points, etc.).
- `Statistik`: tracking of player data and metrics.

### Collections (`SnapshotMap`)

For data read at high frequency (move events, packet listeners), `SnapshotMap` provides lock-free, thread-safe
iteration. See the [module benchmarks](./snapshot-map) for measured comparisons against `ConcurrentHashMap`.

## Getting started

### Prerequisites

- Java 21+
- Gradle (Kotlin DSL)
- A Paper/Spigot server 1.21.1+

### Installation

All internal libraries live in this repository, so a plain clone is enough:

```shell
git clone https://github.com/Aquatic-gg/Waves.git
```

### Building

```shell
# Build the main shadowed jar
./gradlew shadowJar
```

## Contributing

To contribute to a specific module, see the individual module directories linked in the table above.

## Community & Support

- Discord: [Aquatic Development](https://discord.com/invite/ffKAAQwNdC)
- Issues: open a ticket on GitHub for bugs or feature requests.
