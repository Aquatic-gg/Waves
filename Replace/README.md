# Replace 🔄

[![CodeFactor](https://www.codefactor.io/repository/github/mrlarkyy/replace/badge)](https://www.codefactor.io/repository/github/mrlarkyy/replace)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/replace/Replace?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/replace/Replace)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

**Replace** is a high-performance, type-safe Kotlin library designed for Minecraft plugins to handle dynamic placeholders with built-in caching and smart state management.

## ✨ Key Features

- **Smart Updating:** Automatically avoids redundant updates to save CPU and network bandwidth (crucial for packet-based systems).
- **Type-Safe Contexts:** Link placeholders to specific types (e.g., `Player`, `Entity`, or custom objects).
- **Context Transformations:** Easily map data types (e.g., provide a `Game` object and automatically inherit `Player` placeholders).
- **Update Intervals:** Built-in throttling to control how often values are re-calculated.
- **Multi-Format:** Support for `String` literals and Kyori `Component`s out of the box.

---

## 📦 Installation

Add the repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    implementation("gg.aquatic.replace:Replace:1.0.0")
}
```

---

## 🚀 Quick Start

### 1. Define Placeholders
You can define placeholders that return simple strings or complex Kyori components.

```kotlin
// A constant placeholder (only calculated once per session)
val playerName = Placeholder.Literal<Player>("name", isConst = true) { player, _ -> 
    player.name 
}

// A dynamic placeholder with internal arguments (e.g., %stat_kills%)
val statPlaceholder = Placeholder.Literal<Player>("stat", isConst = false) { player, arg ->
    when (arg.lowercase()) {
        "kills" -> getKills(player).toString()
        "deaths" -> getDeaths(player).toString()
        else -> "0"
    }
}
```

### 2. Global Registration
Register placeholders globally so they are automatically included when creating new contexts for that type.

```kotlin
Placeholders.register(playerName, statPlaceholder)
```

### 3. Context & Transformations
A `PlaceholderContext` manages the lifecycle of your placeholders. You can transform contexts to reuse existing logic.

```kotlin
class MyGameSession(val player: Player, val score: Int)

// Create a context for MyGameSession that INHERITS all Player placeholders
val gameContext = Placeholders.resolverFor<MyGameSession>(
    maxUpdateInterval = 20, // 20 ticks
    transforms = arrayOf(
        Placeholders.Transform { it.player } // Tell the context how to get a Player from a MyGameSession
    )
)
```

### 4. Efficient Updating
The library uses a "State" system. You can check if a value actually changed before sending updates to a player.

```kotlin
val component = Component.text("Welcome %name%! Kills: %stat_kills%")
val contextItem = gameContext.createItem(mySession, component)

// Attempt to update (respects maxUpdateInterval)
val updateResult = contextItem.tryUpdate(mySession)

if (updateResult.wasUpdated) {
    val newComponent = updateResult.value
    player.sendMessage(newComponent)
}
```

## 🔌 PlaceholderAPI Support
If PlaceholderAPI is present on the server, `Replace` can automatically wrap PAPI placeholders into its type-safe system using the `papi` identifier:

`%papi_player_name%` → Automatically resolved via PAPI.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 💬 Community & Support

Got questions, need help, or want to showcase what you've built with **KEvent**? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

*   **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
*   **Issues**: Open a ticket on GitHub for bugs or feature requests.


---
*Built with ❤️ by Larkyy*