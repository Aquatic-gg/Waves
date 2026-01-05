# Stacked

[![CodeFactor](https://www.codefactor.io/repository/github/mrlarkyy/stacked/badge)](https://www.codefactor.io/repository/github/mrlarkyy/stacked)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/Stacked?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/Stacked)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

**Stacked** is a robust Kotlin library for Minecraft (Paper/Spigot) developers, designed to provide a unified
abstraction layer for item management. It simplifies handling, serializing, and modifying items across various popular
custom item plugins.

## 🚀 Key Features

- Unified Item API: Interface with multiple item providers through a single, consistent API.
- Cross-Plugin Support: Native support for Oraxen, Nexo, ItemsAdder, MythicMobs, MMOItems, HeadDatabase, Eco, and
  CraftEngine.
- Rich Item Options: Granular control over item properties including Lore, Enchants, Custom Model Data, Dye colors, and
  Spawner types via ItemOptionHandle.
- Serialization Layer: Seamlessly serialize and deserialize items for database storage or configuration files.
- Interaction Handling: Built-in event system to handle complex item interactions (clicks, drops, swaps) easily.

---

## 📦 Installation

Add the repository and dependency to your build.gradle.kts:

````kotlin
repositories {
    maven {
        name = "aquatic-releases"
        url = uri("https://repo.nekroplex.com/releases")
    }
}

dependencies {
    compileOnly("gg.aquatic.stacked:Stacked:26.0.1")
}
````

---

## 💡 Quick Start

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

## 🛠️ Supported Factories

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

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 💬 Community & Support

Got questions, need help, or want to showcase what you've built with **KEvent**? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

* **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
* **Issues**: Open a ticket on GitHub for bugs or feature requests.

---
*Built with ❤️ by Larkyy*