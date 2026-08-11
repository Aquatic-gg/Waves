# Stacked

[![CodeFactor](https://www.codefactor.io/repository/github/mrlarkyy/stacked/badge)](https://www.codefactor.io/repository/github/mrlarkyy/stacked)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/stacked?color=40c14a&name=Reposilite)](https://repo.aquatic.gg/#/releases/gg/aquatic/stacked)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

Stacked is a Kotlin library for Minecraft (Paper/Spigot) that provides an abstraction layer for item management. It
handles, serializes, and modifies items across several custom item plugins through one API.

## Key Features

- Unified item API: interface with multiple item providers through a single API.
- Cross-plugin support: Oraxen, Nexo, ItemsAdder, MythicMobs, MMOItems, HeadDatabase, Eco, and CraftEngine.
- Item options: control over item properties including lore, enchants, custom model data, dye colors, and spawner types
  via `ItemOptionHandle`.
- Serialization: serialize and deserialize items for database storage or configuration files.
- Interaction handling: event system for item interactions (clicks, drops, swaps).

---

## Installation

Add the repository and dependencies to your build.gradle.kts:

````kotlin
repositories {
  maven {
    name = "aquatic-releases"
    url = uri("https://repo.aquatic.gg/releases")
  }
}

dependencies {
  implementation("gg.aquatic:stacked:26.0.54")

  implementation("gg.aquatic:kregistry:26.0.54")
  implementation("gg.aquatic:kevent:26.0.54")
}
````

---

## Quick Start

### Initialization

You must initialize the library with your plugin instance and a CoroutineScope:

````kotlin
initializeStacked(myPlugin, myCoroutineScope)
Stacked.injectFactories()
````

### Registering an Item with Logic

You can register items with unique IDs and attach interaction handlers directly:

````kotlin
val stackedItem = ... // Create or load your StackedItem
stackedItem.register("my_namespace", "my_item_id") { event ->
    event.player.sendMessage("You clicked a custom item!")
}
````

### Retrieving a StackedItem

Retrieve items from the registry at any time:

````kotlin
val item = StackedItem.ITEMS["my_namespace:my_item_id"]
val itemStack = item?.getItem()
````

### Serializing Items

You can load items easily from your configuration files:

````kotlin
val item = StackedItem.loadFromYml(ConfigurationSection)
````

---

## Supported Factories

Stacked uses specialized factories to retrieve items from different providers:

- Base64
- CraftEngine
- Eco
- HeadDatabase
- ItemsAdder
- MMOItems
- MythicMobs
- Nexo
- Oraxen
- Registry Items

---

## Contributing

Contributions are welcome. Feel free to submit a pull request.

---

## Community & Support

- Discord: [Aquatic Development](https://discord.com/invite/ffKAAQwNdC)
- Issues: open a ticket on GitHub for bugs or feature requests.
