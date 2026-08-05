# 🌊 Waves Framework

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/waves/badge)](https://www.codefactor.io/repository/github/mrlarkyy/waves)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/waves?color=40c14a&name=Reposilite)](https://repo.aquatic.gg/#/releases/gg/aquatic/waves)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

**Waves** is a cutting-edge, modular development ecosystem for high-scale Minecraft servers. Built from the ground up to
leverage **Kotlin Coroutines** and **Packet-Level Abstractions**, it allows developers to build feature-rich
experiences (like fake entities, custom GUIs, and scripted logic) without the performance tax of traditional Bukkit API
implementations.

---

## 🏛 Core Philosophy: "Client-Side First"

Traditional Minecraft development relies on server-side entities that tick every 50ms, consuming valuable CPU cycles.
Waves shifts the paradigm:

- **Zero-Tick Visuals**: Using the `Pakket` module, visuals like Holograms and Fake Entities exist only in the player's
  memory.
- **Asynchronous Logic**: Nearly every system in Waves (Actions, Database, Input) is designed to run on `Dispatchers.IO`
  or custom coroutine scopes, keeping the main server thread focused strictly on physics and vital logic.
- **Snapshot Caching**: High-read data is stored in `SnapshotMap`, providing near-instant access for multi-threaded read
  operations.

---

## 🧱 The Module Ecosystem

Waves is powered by a comprehensive family of specialized libraries. Each module handles a specific pillar of modern
plugin development, allowing for a clean, decoupled architecture.

| Module                                                         | Purpose                                                                                                                           | Source                                                |
|:---------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------|
| **[AquaticCommon](./aquatic-common)** | The foundation. Provides `ArgumentContext` for type-safe data parsing, Coroutine scopes, and extensive Bukkit extensions.         | [🔗 Source](./aquatic-common) |
| **[Pakket](./pakket)**               | High-level NMS/Packet abstraction. Manages client-side entity spawning, metadata updates, and passenger packets.                  | [🔗 Source](./pakket)        |
| **[Execute](./execute)**             | A serializable logic engine. Allows complex `Actions` and `Requirements` to be defined in configurations and executed at runtime. | [🔗 Source](./execute)       |
| **[KMenu](./kmenu)**                 | A reactive DSL for Inventory GUIs. Features asynchronous click handling, pagination, and dynamic button updating.                 | [🔗 Source](./kmenu)         |
| **[Kommand](./kommand)**             | Modern, type-safe command routing framework that eliminates boilerplate command registration.                                     | [🔗 Source](./kommand)       |
| **[KLocale](./klocale)**             | Internationalization engine. Handles per-player localization with full Adventure/MiniMessage support.                             | [🔗 Source](./klocale)       |
| **[SnapshotMap](./snapshot-map)**     | Thread-safe, lock-free maps optimized for extreme read performance in high-concurrency environments.                              | [🔗 Source](./snapshot-map)   |
| **[KRegistry](./kregistry)**         | Dynamic object registry for managing lifecycles and lookups of custom plugin components.                                          | [🔗 Source](./kregistry)     |
| **[KEvent](./kevent)**               | Lightweight, Coroutine-friendly event wrappers to replace standard, bulky event listeners.                                        | [🔗 Source](./kevent)        |
| **[Kurrency](./kurrency)**           | A unified economy abstraction layer supporting multiple providers (Vault, PlayerPoints, etc.) and custom currencies.              | [🔗 Source](./kurrency)      |
| **[Blokk](./blokk)**                 | A simple block library for getting blockdata & placing blocks via multiple plugins.                                               | [🔗 Source](./blokk)         |
| **[Replace](./replace)**             | High-performance string replacement engine designed for rapid placeholder processing.                                             | [🔗 Source](./replace)       |
| **[Stacked](./stacked)**             | Modern ItemStack utility library for click handling and item building with plugin hooks.                                          | [🔗 Source](./stacked)       |
| **[Statistik](./statistik)**         | Optimized data tracking system for player metrics.                                                                                | [🔗 Source](./statistik)     |
| **[TreePAPI](./tree-papi)**           | A placeholders DSL for PlaceholderAPI plugin - simple creation of placeholders.                                                   | [🔗 Source](./tree-papi)      |

---

## 🛠 Feature Deep-Dive

### 👻 Packet-Based "Fake" Objects

Located in `gg.aquatic.waves.clientside`, this system allows for:

- **Client-Side Entities**: Spawn NPCs, blocks, or models that only specific players can see.
- **ModelEngine Integration**: Built-in support for `FakeMEG` to handle custom models via packets.
- **Interaction Handling**: Packet-level click detection that maps back to standard Bukkit-like events.

### 📜 Scriptable Actions (`Execute`)

Turn your YAML configs into logic. `Execute` supports:

- **Arguments**: Type-safe parameter parsing (Int, String, Collection, etc.) for actions.
- **Requirements**: Gate actions behind checks (permissions, currency, etc.).
- **Smart Actions**: Nested logical conditions directly in configuration.

### 💰 Economy & Data (`Kurrency` & `Statistik`)

- **Kurrency**: A unified API for handling multiple types of currency (Vault, Points, etc.).
- **Statistik**: Optimized tracking of player data and metrics.

### 🗺 Optimized Collections (`SnapshotMap`)

For data that is read thousands of times per second (like move events or packet listeners), Waves uses `SnapshotMap` to
provide lock-free, thread-safe access that outperforms standard `ConcurrentHashMap`.

---

## ⚡ Performance Benchmarks

The **SnapshotMap** module is specifically benchmarked for Minecraft environments.

- **Read Speed**: Significantly faster than `ConcurrentHashMap` for high-frequency lookups (e.g., getting player data on
  every move packet).
- **Scalability**: Designed to maintain performance as player counts increase.

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Gradle (with Kotlin DSL)
- A Paper/Spigot server 1.21.1+

### Installation

All internal libraries live in this repository, so a plain clone is all you need:

```shell script
git clone https://github.com/Aquatic-gg/Waves.git
```

### Building

```shell script
# Build the main shadowed jar
./gradlew shadowJar
```

## 🤝 Contributing

Waves is a massive ecosystem. If you'd like to contribute to a specific module, please check the individual module
directories linked in the table above.

---

## 💬 Community & Support

Got questions, need help, or want to showcase what you've built with **Waves**? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

* **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
* **Issues**: Open a ticket on GitHub for bugs or feature requests.