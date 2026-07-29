# Kurrency

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/kurrency/badge)](https://www.codefactor.io/repository/github/mrlarkyy/kurrency)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/Kurrency?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/Kurrency)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

Kurrency is a powerful, flexible, and asynchronous multi-currency management library designed for high-performance
Minecraft servers (Paper/Spigot). It provides a robust API for handling player balances with support for multiple
database backends and built-in caching.

## ✨ Features

- **Multi-Currency Support**: Register and manage multiple custom currencies simultaneously.
- **Asynchronous & Thread-Safe**: Built with Kotlin Coroutines and mutex-based locking to ensure data integrity during
  transactions.
- **Database Persistence**: Powered by [JetBrains Exposed](https://github.com/JetBrains/Exposed), supporting various SQL
  databases.
- **Efficient Caching**: Customizable caching layer to reduce database load.
- **Registry System**: Integrated with `KRegistry` for easy currency management and lookup.
- **Event System**: Built-in event bus (using `KEvent`) to listen for currency transactions.
- **Vault Integration**: Ready for interoperability with the Vault API.

---

## 🚀 Getting Started

### Installation

Add Kurrency to your project via Maven or Gradle.

**Gradle (Kotlin DSL):**

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    implementation("gg.aquatic:Kurrency:26.0.1")
}
```

### Initialization

Choose your preferred cache implementation and initialize the library:

```kotlin
// Example using Redis cache
val cache = RedisCurrencyCache(jedis, dbHandler, ttlSeconds = 1800)

initializeKurrency(
    dbUrl = "jdbc:mysql://localhost:3306/kurrency",
    dbDriver = "com.mysql.cj.jdbc.Driver",
    dbUser = "user",
    dbPass = "password",
    cache = cache,
    currencies = listOf(VaultCurrency()) // Register initial currencies
)
```

## 🛠 Currency Types & Registration

Kurrency supports several ways to handle money. You can register them during initialization or at runtime using
`KurrencyConfig.injectCurrency(currency)`.

### 1. Virtual Currency (Database-backed)

Virtual currencies are stored in the database. Use this for "Coins", "Gems", etc.

```kotlin
// Define the currency metadata
val gems = VirtualCurrency(
    id = "gems",
    prefix = "&a💎 ",
    suffix = " Gems"
).register() // Automatically registers it to the Kurrency Registry
```

### 2. Item Currency (Physical-backed)

Item currencies represent physical items in the player's inventory (e.g., gold ingots or custom tokens).

```kotlin
val tokenItem = ItemStack(Material.SUNFLOWER).apply {
    itemMeta = itemMeta?.apply { setDisplayName("§eEvent Token") }
}

val eventTokens = ItemCurrency(
    id = "event_tokens",
    item = tokenItem,
    prefix = "§e",
    suffix = " Tokens"
)

// Manually inject if not passed during initialization
KurrencyConfig.injectCurrency(eventTokens)
```

### 3. Vault Bridge

If you want to interact with the server's primary economy (e.g., EssentialsX) through Kurrency:

```kotlin
val vault = VaultCurrency(id = "money", prefix = "$")
KurrencyConfig.injectCurrency(vault)
```

## 💻 Usage Examples

### Basic Operations

All operations are `suspend` functions and should be called within a Coroutine scope.

```kotlin
val currency = KurrencyConfig.getCurrency("gems") ?: return

// Give a player balance
currency.give(player, BigDecimal("100.00"))

// Safely attempt to take balance (returns false if insufficient funds)
val success = currency.tryTake(player, BigDecimal("50.0"))

// Get a formatted string: "💎 1,250.00 Gems"
val display = currency.formatBalance(BigDecimal("1250"))
```

### Safe Transactions (Virtual Only)

For database-backed currencies, use the `CurrencyHandler` to ensure thread-safety during complex logic:

```kotlin
val handler = KurrencyConfig.currencyHandler

// Transactional lock ensures no other changes happen to this player's gems during the block
handler.withTransaction(player.uniqueId, gems) { currentBalance ->
    if (currentBalance > BigDecimal("1000")) {
// Business logic here...
    }
}
```

---

## 💬 Community & Support

Got questions, need help, or want to showcase what you've built with **Kurrency**? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

*   **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
*   **Issues**: Open a ticket on GitHub for bugs or feature requests.