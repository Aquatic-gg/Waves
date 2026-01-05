# Execute

[![CodeFactor](https://www.codefactor.io/repository/github/mrlarkyy/execute/badge)](https://www.codefactor.io/repository/github/mrlarkyy/execute)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/execute?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/execute)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

Execute is a high-performance, coroutine-based engine for creating complex logic sequences. It allows you to bind **Actions** to **Conditions** using a robust, extensible argument and registry system.

---

### 📦 Installation

Add the repository and dependency to your `build.gradle.kts`:

````kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    implementation("gg.aquatic.execute:Execute:1.0.2")
}
````

---

### 🚀 Quick Start

Initialize the engine in your plugin's `onEnable`. This sets up the Adventure API (MiniMessage) integration and registers all default executables.

````kotlin
// Initialize global state
initExecute(this)

// Injects default actions (sound, title, command, etc.) 
// and conditions (permission)
Execute.injectExecutables()
````

---

### 🛠️ Core Concepts & Code Examples

#### 1. Creating a Custom Action
Actions define logic executed on a "binder" (usually a Player).

````kotlin
object MyCustomAction : Action<Player> {
    override val arguments = listOf(
        PrimitiveObjectArgument("message", "Default Hello", true)
    )

    override suspend fun execute(binder: Player, args: ArgumentContext<Player>) {
        val msg = args.string("message") ?: return
        binder.sendMessage(msg)
    }
}
````

#### 2. Creating a Custom Condition
Conditions return a Boolean. The engine's `ConditionHandle` automatically handles the `negate` argument for you.

````kotlin
object MyCondition : Condition<Player> {
    override val arguments = listOf(
        PrimitiveObjectArgument("value", 10, true)
    )

    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean {
        val value = args.int("value") ?: return false
        return binder.level >= value
    }
}
````

---

### 💾 Serialization System

Execute features a built-in serialization layer via `ActionSerializer`, `ConditionSerializer`, and `ArgumentSerializer`. This allows for seamless conversion between configuration files (like YAML) and live objects.

> #### How Serialization Works:
> * **Config to Object:** The `ArgumentSerializer` reads `ConfigurationSection` data and maps it to the defined `ObjectArgument` list.
> * **Nested Logic:** It can recursively serialize `ActionsArgument` and `ConditionsArgument`, allowing for complex nested logic in your config files.
> * **Memory Configuration:** Uses the `ConfigExt.kt` utilities to handle maps and lists inside Bukkit's `MemoryConfiguration`.

**Config Example:**
````yaml
my-button:
  actions:
    - type: "sound"
      sound: "entity.experience_orb.pickup"
      volume: 1.0
    - type: "command"
      player-executor: true
      command: "say I clicked the button!"
  conditions:
    - type: "permission"
      permission: "myplugin.use"
      negate: false
````

---

### 📂 Advanced Argument Types

The engine supports a wide array of specialized arguments:
* **TimedActionsArgument:** Execute actions after a specific delay.
* **Vector / VectorListArgument:** Precise coordinate handling for world effects.
* **UpdatableObjectArgument:** Arguments that can change their value based on external factors during runtime.

---

### 🏗️ Project Structure

* `gg.aquatic.execute.action.impl.logical`: Advanced execution logic (ConditionalActions, SmartActions).
* `gg.aquatic.execute.argument.impl`: Implementation of the argument parsing engine.
* `gg.aquatic.execute.requirement`: Core logic for conditional checks and failure handling.
* `gg.aquatic.execute.coroutine`: Dispatchers for safe Minecraft thread management.

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