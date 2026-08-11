# Blokk

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/blokk/badge)](https://www.codefactor.io/repository/github/mrlarkyy/blokk)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/blokk?color=40c14a&name=Reposilite)](https://repo.aquatic.gg/#/releases/gg/aquatic/blokk)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

Blokk is a Kotlin library for Minecraft (Paper/Spigot) that provides an abstraction layer for block placement. It handles
Vanilla, ItemsAdder, and Oraxen blocks through a single API, and supports rotatable multi-block structures.

## Features

- **Unified block API**: A single interface for `Vanilla`, `ItemsAdder`, and `Oraxen` blocks.
- **Multi-block structures**: Define shapes using a character-map grid system.
- **Directional rotation**: Multi-blocks rotate based on the target location's yaw.
- **Serialization**: Load individual blocks or multi-blocks from a Bukkit `ConfigurationSection`.
- **Extensible**: Add custom block providers through the `BlockFactory` registry.

## Installation

Add the repository and dependency to your `build.gradle.kts`:

````kotlin
repositories {
    maven("https://repo.aquatic.gg/releases")
}

dependencies {
    implementation("gg.aquatic:blokk:26.0.54")
}
````

## Usage

### Initialization
Initialize the library with the block factories you want to support (e.g. ItemsAdder or Oraxen).

````kotlin
override fun onEnable() {
    initializeBlokk(mapOf(
        "itemsadder" to IAFactory,
        "oraxen" to OraxenFactory
    ))
}
````


### Loading and Placing Blocks
The `BlokkSerializer` automatically detects the block type based on the material prefix (e.g., `itemsadder:` or `oraxen:`).

````kotlin
// In your config.yml
// my-block:
//   material: "oraxen:cave_crystal"

val section = config.getConfigurationSection("my-block")!!
val block = BlokkSerializer.load(section)

block.place(location)
````

### Multi-Block Structures
Multi-blocks are defined using layers. Each character represents a block defined in the `blocks` section.

````yaml
# structure.yml
blocks:
  'S':
    material: "STONE"
  'G':
    material: "GOLD_BLOCK"
layers:
  0: # Y-offset 0
    -1: "SSS" # Z-offset -1
    0: "SGS" # Z-offset 0
    1: "SSS" # Z-offset 1
````

````kotlin
val multiBlock = BlokkSerializer.loadMultiBlock(configSection)

// Spawns the structure relative to the location and its orientation
val placedLocations = multiBlock.spawn(location)
````

## Community & Support

- Discord: [Aquatic Development](https://discord.com/invite/ffKAAQwNdC)
- Issues: open a ticket on GitHub for bugs or feature requests.