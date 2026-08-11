# KLocale

[![CodeFactor](https://www.codefactor.io/repository/github/mrlarkyy/klocale/badge)](https://www.codefactor.io/repository/github/mrlarkyy/klocale)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/klocale?color=40c14a&name=Reposilite&filter=26)](https://repo.aquatic.gg/#/releases/gg/aquatic/klocale)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

KLocale is a localization library for Kotlin and PaperMC. It maps configuration files to Adventure Components.

## Features

*   **Pre-rendering:** Static messages are pre-rendered to reduce object allocation.
*   **Fallbacks:** Automatic locale resolution (e.g. en_US -> en -> default).
*   **MiniMessage:** Supports Kyori MiniMessage and legacy color codes.
*   **Single-pass replacement:** Placeholder replacement avoids double-replacement issues.
*   **Multiple providers:** Load locales from YAML, GitHub, HTTP, or internal resources.
*   **Async loading:** Coroutine-based loading to keep locale loading off the main thread.
*   **Missing keys:** Configurable strategies for missing keys (`MissingKeyHandler`).

---

## Installation

Add the library to your build.gradle.kts:

````kotlin
repositories {
    maven("https://repo.aquatic.gg/releases")
}

dependencies {
    implementation("gg.aquatic:klocale:26.0.54")
    implementation("gg.aquatic:klocale-paper:26.0.54")
}
````

---

## Quick Start (Paper)

Initialize your locale manager using the Kotlin DSL:

````kotlin
val localeManager = KLocale.paper {
    defaultLanguage = "en"

    // Add one or more providers
    providers += YamlLocaleProvider(
        file = File(dataFolder, "lang.yml"),
        serializer = YamlLocaleProvider.DefaultSerializer
    )
    // Optional: Use a custom MiniMessage instance (comes with 'ccmd' tag by default!)
    // miniMessage = MiniMessage.miniMessage()

    // Optional: Handle missing keys gracefully instead of throwing exceptions
    missingKeyHandler = MissingKeyHandler.Throwing()
}

// Reload locales (suspended call)
scope.launch {
    localeManager.invalidate()
}
````

### Sending Messages

Fetching and sending messages is chainable:

````kotlin
fun welcome(player: Player) {
    localeManager.getOrDefault(player.locale(), "welcome-message")
        .replace("player", player.name)
        // Native support for replacing placeholders with rich Components
        .replace("balance", Component.text("$500", NamedTextColor.GREEN))
        .send(player)
}
````

## Advanced Usage

### Type-Safe Enums
Implement `CfgMessageHandler` to access your messages globally.
See `MessagesExample` for a full example.

```kotlin
enum class Messages(override val path: String) : CfgMessageHandler<PaperMessage> {
    WELCOME("welcome-message"),
    STAFF_LIST("staff-list");

    override val manager: LocaleManager<PaperMessage>
        get() = MyPlugin.localeManager }

// Usage:
Messages.WELCOME.message(player.locale()).replace("player", name).send(player)
```

### Handling Custom Languages
If you need to support languages that aren't constants in java.util.Locale (like Czech, Slovak, or regional dialects), use IETF BCP 47 language tags:

````kotlin
// For Czech
val czech = Locale.forLanguageTag("cs-CZ")

// For custom/internal tags
val custom = Locale.forLanguageTag("pirate")

// Usage with manager
localeManager.getOrDefault(czech, "welcome-key")
````

### Custom Callbacks
Attach logic directly to messages (useful for logging or triggering events):

````kotlin
message.withCallback { player, msg -> 
    plugin.logger.info("Sent ${msg.lines.size} lines to ${player.name}")
}.send(player)
````

### Merging Multiple Sources
The MergedLocaleProvider allows you to combine base translations with local user overrides:

````kotlin
val localeManager = KLocale.paper(plugin) {
    // You can specify your own minimessage with custom tag resolvers
    miniMessage = MiniMessage.miniMessage()
    provider = MergedLocaleProvider(
        listOf(
            // 1. Remote "Base" translations
            GitHubLocaleProvider("User", "Repo", "path/to/locales", serializer),

            // 2. Local "User" overrides (Takes priority)
            YamlLocaleProvider(File(dataFolder, "overrides.yml"), serializer)
        )
    )
}
````

### Missing Key Strategies
You can define what happens when a key is missing by implementing MissingKeyHandler. The default behavior is to throw an exception, but you can override this globally:

````kotlin
class MyCustomHandler : MissingKeyHandler<PaperMessage> {
    override fun handle(key: String, language: String): PaperMessage {
        return PaperMessage.of(Component.text("Missing: $key", NamedTextColor.RED))
    }
}
````

---

## Contributing

Contributions are welcome. Feel free to submit a pull request.

---

## Community & Support

- Discord: [Aquatic Development](https://discord.com/invite/ffKAAQwNdC)
- Issues: open a ticket on GitHub for bugs or feature requests.