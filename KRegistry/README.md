# KRegistry

[![CodeFactor](https://www.codefactor.io/repository/github/mrlarkyy/kregistry/badge)](https://www.codefactor.io/repository/github/mrlarkyy/kregistry)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/KRegistry?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/KRegistry)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A lightweight, type-safe registry system for Kotlin. KRegistry provides a bootstrap-driven registry graph with hierarchical type lookups and typed collections.

## Features

*   **Bootstrap-Driven Registries:** Build a registry graph from contributions with deterministic initialization.
*   **Atomic Global State:** The registry graph uses compare-and-swap for safe concurrent updates.
*   **Hierarchical Lookups:** Search grouped registries by binder type (including superclasses).
*   **Zero Boilerplate:** Reified generics for clean, type-safe accessors.

---

## Installation

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    implementation("gg.aquatic:kregistry:25.0.2")
}
```

---

## Getting Started

### Bootstrap + Access

```kotlin
val SERVICES = RegistryKey.simple<String, Service>(RegistryId("core", "services"))

object AppBootstrap : BootstrapHolder
object CoreHolder : RegistryHolder

fun bootstrap() {
    // Build registries
    val build = AppBootstrap.inject()
    
    // Register contributions
    CoreHolder.registryBootstrap(AppBootstrap) {
        registry(SERVICES) {
            add("auth", AuthService())
            add("db", DatabaseService())
        }
    }

    // Finalize registries
    build()
}

// Later...
val services = AppBootstrap[SERVICES]
val auth = services.get("auth")
```

### Grouped Registries (Binder Pattern)

Use a grouped registry when your values are keyed by a binder type (e.g., `Action<Player>`).
Hierarchical lookups include entries registered for supertypes of the requested binder.

```kotlin
interface Action<out B> : GroupedEntry<B>
class Player
data class SendMessage(
    override val binder: Class<out Player>,
    val text: String
) : Action<Player>

val ACTIONS = RegistryKey.grouped<String, Player, Action<out Player>>(
    RegistryId("example", "actions")
)

val contribution: ContributionBuilder.() -> Unit = {
    registry(ACTIONS) {
        add("message", SendMessage(Player::class.java, "hello"))
    }
}

// Later...
val actions = AppBootstrap.get(ACTIONS)
val message = actions.getTypedByClass("message", Player::class.java)
```

## Core Concepts

### The Registry Graph
The registry graph is managed internally. `BootstrapHolder` exposes helpers to build and access registries.

### Grouped Registries
Grouped registries store per-binder registries keyed by a binder class. Use
`RegistryKey.grouped(...)` to create the key and `GroupedRegistry` for accessors.

---

## Community & Support

Got questions, need help, or want to showcase what you've built with KRegistry? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

*   **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
*   **Issues**: Open a ticket on GitHub for bugs or feature requests.
