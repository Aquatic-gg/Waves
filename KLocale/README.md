# 🌍 KLocale

[![CodeFactor](https://www.codefactor.io/repository/github/mrlarkyy/klocale/badge)](https://www.codefactor.io/repository/github/mrlarkyy/klocale)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/KLocale?color=40c14a&name=Reposilite&filter=26)](https://repo.nekroplex.com/#/releases/gg/aquatic/KLocale)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

**KLocale** is a high-performance, developer-friendly localization library for Kotlin and PaperMC. It bridges the gap between raw configuration files and rich, interactive Adventure Components.

## ✨ Features

*   **High Performance:** Pre-renders static messages to minimize object allocation.
*   **Smart Fallbacks:** Automatic locale resolution (e.g., en_US -> en -> default).
*   **MiniMessage Ready:** Native support for Kyori MiniMessage and legacy color codes.
*   **Single-Pass Replacement:** Optimized placeholder system to prevent double-replacement issues.
*   **Multi-Provider:** Load locales from YAML, GitHub, HTTP, or internal resources.
*   **Async Loading:** Coroutine-based loading to keep your server tick-rate silky smooth.
*   **Fail-Safe:** Customizable strategies for missing keys (MissingKeyHandler).

---

## 📦 Installation

Add the library to your build.gradle.kts:

````kotlin
repositories {
    maven("https://repo.aquatic.gg/releases")
}

dependencies {
    implementation("gg.aquatic:KLocale:VERSION")
    implementation("gg.aquatic:KLocale-Paper:VERSION")
}
````

---

## 🚀 Quick Start (Paper)

Initialize your locale manager using the clean Kotlin DSL:

````kotlin
val localeManager = KLocale.paper {
    defaultLanguage = "en"
    provider = YamlLocaleProvider(
        file = File(dataFolder, "lang.yml"),
        serializer = YamlLocaleProvider.DefaultSerializer
    )

    // Optional: Handle missing keys gracefully instead of throwing exceptions
    missingKeyHandler = PaperMissingKeyHandler()
}

// Reload locales (suspended call)
scope.launch {
    localeManager.invalidate()
}
````

### Sending Messages

Fetching and sending messages is intuitive and chainable:

````kotlin
fun welcome(player: Player) {
    localeManager.getOrDefault(player.locale(), "welcome-message")
        .replace("player", player.name)
        // Supports Kyori components
        .replace("balance", "<green>$500</green>".toMMComponent())
        .send(player)
}
````

## 🛠 Advanced Usage

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