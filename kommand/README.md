# Kommand

[![CodeFactor](https://www.codefactor.io/repository/github/mrlarkyy/kommand/badge)](https://www.codefactor.io/repository/github/mrlarkyy/kommand)
[![Core](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/kommand-core?color=40c14a&name=Reposilite&filter=2)](https://repo.aquatic.gg/#/releases/gg/aquatic/kommand-core)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

Kommand is a type-safe Kotlin DSL for command registration on top of Brigadier. It is split into platform modules so the
common DSL can be shared between Paper and Velocity without leaking platform types into the core API.

## Features

* **Type-Safe DSL:** Define commands, subcommands, and arguments using a clean Kotlin syntax.
* **Automatic Mapping:** Map string inputs directly to custom objects using `listArgument`.
* **Advanced Argument Types:**
    * `playerArgument`: Platform-specific extension for Paper and Velocity with optional filters.
    * `listArgument`: For dynamic suggestions and automatic object mapping.
    * `flagsArgument`: Support for CLI-style flags (e.g., `-s`, `--silent`).
    * `namedArguments`: Key-value pair parsing (e.g., `-amount:5`).
* **Contextual Execution:** Access the command sender (like `Player`) directly with type-safe `execute<T>` blocks.
* **Coroutine Support:** Built-in `suspendExecute` for handling asynchronous logic without blocking the main thread.
* **Inheritance:** Subcommands automatically inherit execution logic from parent nodes unless short-circuited.

---

## Installation

Add the platform module you need to your `build.gradle.kts`:

````kotlin
repositories {
    maven("https://repo.aquatic.gg/releases")
}

dependencies {
    implementation("gg.aquatic:kommand-paper:26.0.54")
    // or
    implementation("gg.aquatic:kommand-velocity:26.0.54")
}
````

---

## Getting Started

### Basic Usage
Define a simple command with a required argument:

```kotlin
command("teleport", "tp") {
    hasPermission("myplugin.teleport")

    playerArgument("target") {
        execute<Player> {
            val target = get<Player>("target")
            sender.teleport(target.location)
            sender.sendMessage("Teleported to ${target.name}")
            true
        }
    }
}
```

`stringArgument` now supports `WORD`, `STRING`, and `GREEDY_STRING` through the `format` parameter, so you can keep one API for single words, quoted text, or the rest of the line.

---

## Understanding Execution Flow

In **Kommand**, every `execute` block returns a `Boolean`. This value determines whether the engine should stop or continue searching for more specific matches.

### 1. Short-circuiting vs. Inheritance
*   **`return true`**: Logic stops here. Use this when the command is fully handled.
*   **`return false`**: Logic runs, but the engine continues to check child arguments or subcommands.

This is useful for global logic (like logging or cooldown checks) that should apply to all subcommands:

```kotlin
command("economy") {
    execute<Player> {
        println("${sender.name} used an economy command!")
        false // Returning false ensures the subcommands (balance, pay) still run!
    }

    "balance" {
        execute<Player> {
            sender.sendMessage("Balance: $500")
            true
        }
    }
}
```

### 2. Branching & `execute` Placement
The placement of an `execute` block determines **when** it runs. By moving the block relative to an argument, you can handle optional parameters or "Help" fallbacks.

#### Placement: BEFORE the argument
If `execute` is placed at the same level as an argument but **before** its definition, it acts as the handler for when that argument is **missing**.

```kotlin
command("feed") {
    // This runs for: /feed
    execute<Player> {
        sender.sendMessage("Usage: /feed <player>")
        true
    }

    playerArgument("target") {
        // This runs for: /feed <player>
        execute<Player> {
            val target = get<Player>("target")
            target.foodLevel = 20
            true
        }
    }
}
```

#### Placement: INSIDE the argument
If `execute` is placed **inside** the argument block, it can only run if that argument was successfully parsed.

```kotlin
command("heal") {
  playerArgument("target") {
    execute<Player> {
      // This ONLY runs if a valid player name was provided
      val target = get<Player>("target")
      target.health = 20.0
      true
    }
  }
}
```

---

## Advanced Examples

### Advanced List Mapping
Map dynamic collections to your own domain objects automatically.

```kotlin
data class Kit(val id: String, val weight: Double)
val kitRegistry = listOf(Kit("starter", 10.0), Kit("pro", 2.5))

command("kit") {
    listArgument("kitType", values = { kitRegistry }, mapper = { it.id }) {
        execute<Player> {
            val kit = get<Kit>("kitType") // get<T> returns the actual Kit object
            sender.sendMessage("You chose the ${kit.id} kit (Weight: ${kit.weight})")
            true
        }
    }
}
```

### Flags and Named Arguments
```kotlin
command("broadcast") {
    stringArgument("message", format = StringArgumentFormat.GREEDY_STRING) {
        flagsArgument("opts", listOf("-s", "--silent")) {
            execute<Player> {
                val msg = string("message")
                if (hasFlag("opts", "--silent")) { /* silent logic */ }
                true
            }
        }
    }
}
```

### Asynchronous Execution (Coroutines)
```kotlin
command("stats") {
  suspendExecute<Player> {
    // Runs in CoroutineScope (Dispatchers.Default)
    val data = myDatabase.loadData(sender.uniqueId)
    sender.sendMessage("Your Rank: ${data.rankName}")
  }
}
```

---

## Contributing

Contributions are welcome. Feel free to submit a pull request.

---

## Community & Support

- Discord: [Aquatic Development](https://discord.com/invite/ffKAAQwNdC)
- Issues: open a ticket on GitHub for bugs or feature requests.
