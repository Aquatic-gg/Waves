# Replace

[![CodeFactor](https://www.codefactor.io/repository/github/mrlarkyy/replace/badge)](https://www.codefactor.io/repository/github/mrlarkyy/replace)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/replace?color=40c14a&name=Reposilite)](https://repo.aquatic.gg/#/releases/gg/aquatic/replace)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

Replace is a type-safe Kotlin library for handling dynamic placeholders in Minecraft plugins, with caching and change
tracking to avoid redundant work.

## Key Features

- **Change tracking:** Skips redundant updates to save CPU and network bandwidth, which matters for packet-based systems.
- **Type-safe contexts:** Link placeholders to specific types (e.g. `Player`, `Entity`, or custom objects).
- **Context transformations:** Map data types (e.g. provide a `Game` object and inherit `Player` placeholders).
- **Update intervals:** Throttling to control how often values are recalculated.
- **Multiple formats:** Supports `String` literals and Kyori `Component`s.

---

## Installation

Add the repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.aquatic.gg/releases")
}

dependencies {
    implementation("gg.aquatic:replace:26.0.54")
}
```

---

## Quick Start

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

### 5. Placeholder DSL
For placeholders with multiple branches and arguments (PAPI-style), use the built-in DSL. It handles underscored tokens
and quoted arguments automatically.

```kotlin
Placeholders.registerDSL<Player>("rank") {
    // %rank_name%
    "name" {
        handle { getRankName(binder) }
    }
    
    // %rank_info_<rank>%
    "info" {
        stringArgument("target_rank") {
            handle { 
                val rank = string("target_rank")
                "Details for $rank..." 
            }
        }
    }

    // Optional arguments logic: %rank_status% or %rank_status_detailed%
    "status" {
        handle { "Simple Status" }
        "detailed" {
            handle { "Very Detailed Status" }
        }
    }
}
```

Example with shared handler

```kotlin
Placeholders.registerDSL<Player>("stat") {
    // Shared handler for both %stat_wins% and %stat_wins_<player>%
    "wins" {
        handle {
            val targetName = string("player") ?: binder.name
            getWins(targetName).toString()
        }
        stringArgument("player") {
            // No handler needed here; it automatically falls back to the parent
        }
    }
}
```

## PlaceholderAPI Support
If PlaceholderAPI is present on the server, Replace can wrap PAPI placeholders into its type-safe system using the `papi`
identifier:

`%papi_player_name%` -> resolved via PAPI.

---

## Contributing

Contributions are welcome. Feel free to submit a pull request.

---

## Community & Support

- Discord: [Aquatic Development](https://discord.com/invite/ffKAAQwNdC)
- Issues: open a ticket on GitHub for bugs or feature requests.