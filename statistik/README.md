# Statistik

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/statistik/badge)](https://www.codefactor.io/repository/github/mrlarkyy/statistik)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/statistik?color=40c14a&name=Reposilite)](https://repo.aquatic.gg/#/releases/gg/aquatic/statistik)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

**Statistik** is a lightweight, extensible Kotlin framework for tracking player statistics on Minecraft (Paper/Spigot)
servers. It provides a structured way to register, listen for, and handle various player actions through a clean,
argument-based system.

## 🚀 Features

- **Modular Design:** Easy to add new statistic types by extending `ListenerStatisticType`.
- **Argument Support:** Statistics can accept dynamic arguments (e.g., filtering `BlockBreak` by specific block types).
- **Event-Driven:** Uses a custom `StatisticAddEvent` to bridge game events with your statistic logic.
- **Kotlin-First:** Built with Kotlin, leveraging DSL-like listeners and objects for a clean API.

---

## 📦 Installation

To use Statistik in your project, add it to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.aquatic.gg/releases")
}

dependencies {
    implementation("gg.aquatic:statistik:26.0.54")
}
```

---

## 💻 Usage

### Registering a Statistic

Statistics are managed through the `StatistikRegistry`. You can register the built-in implementations or your own custom
ones.

### Implementing a Custom Statistic

### Implementing a Custom Statistic

Creating a new statistic is simple. While `ListenerStatisticType` is available for Bukkit events, you can extend the base `StatisticType` for any custom logic:

```kotlin
object MyCustomStatistic : StatisticType<Player>() {
    override val arguments = listOf(/* Your arguments here */)

    override fun initialize() {
        // Logic to run when the first handle is registered
        // (e.g., starting a repeating task)
    }

    override fun terminate() {
        // Logic to run when the last handle is unregistered
        // (e.g., stopping a repeating task)
    }
}
```

### Example: Creating and Listening to Statistics

#### 1. Manual Handle Creation

If you want to track specific blocks programmatically, you can create a handle manually. The `consumer` block is called
whenever the statistic criteria are met.

```kotlin
// Define the arguments (e.g., filter for Diamond and Gold blocks)
val argsMap = mapOf("types" to listOf("DIAMOND_ORE", "GOLD_ORE"))
val args = ObjectArguments(argsMap)

// Create the handle for BlockBreakStatistic
val handle = StatisticHandle(BlockBreakStatistic, args) { event ->
    val player = event.binder
    val amount = event.increasedAmount

    player.sendMessage("You mined a block! Progress increased by $amount")
}

// Start listening
handle.register()

// Stop listening when no longer needed
// handle.unregister()
```

#### 2. Using the Serializer (Recommended)

Usually, you'll want to define statistics in a `config.yml`. The `StatistikSerializer` makes this easy by mapping
configuration sections directly to handles.

**config.yml:**

```yaml
my-stats:
  - type: "block-break"
    types:
      - "STONE"
      - "COBBLESTONE"
  - type: "death"
```

**Kotlin Code:**

```kotlin
val section = config.getConfigurationSection("my-stats") ?: return

// Load all handles from the configuration
val handles = StatistikSerializer.fromSections<Player>(section.getSections()) { event ->
    // This logic runs for EVERY statistic defined in that config section
    println("${event.binder.name} just triggered ${event.statistic::class.simpleName}!")
}

// Register them all to start tracking
handles.forEach { it.register() }
```

### 💡 Key Concepts

- **`StatisticType`**: The logic provider (e.g., `BlockBreakStatistic`). It defines *what* event to listen to.
- **`StatisticHandle`**: An instance of a statistic. It defines *filters* (via `ObjectArguments`) and *actions* (via the
  `consumer`).
- **`StatisticAddEvent`**: The data packet passed to the consumer, containing the player (`binder`) and the
  `increasedAmount` (e.g., damage dealt or blocks broken).

---

## 💬 Community & Support

Got questions, need help, or want to showcase what you've built with **Statistik**? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

*   **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
*   **Issues**: Open a ticket on GitHub for bugs or feature requests.