# KRegistry

[![CodeFactor](https://www.codefactor.io/repository/github/mrlarkyy/kregistry/badge)](https://www.codefactor.io/repository/github/mrlarkyy/kregistry)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/kregistry?color=40c14a&name=Reposilite)](https://repo.aquatic.gg/#/releases/gg/aquatic/kregistry)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A type-safe registry system for Kotlin. KRegistry provides a bootstrap-driven registry graph with hierarchical type lookups and typed collections.

## Features

*   **Bootstrap-driven registries:** Build a registry graph from contributions with deterministic initialization.
*   **Atomic global state:** The registry graph uses compare-and-swap for concurrent updates.
*   **Hierarchical lookups:** Search grouped registries by binder type, including superclasses.
*   **Reified generics:** Type-safe accessors without manual casting.

---

## Installation

```kotlin
repositories {
    maven("https://repo.aquatic.gg/releases")
}

dependencies {
    implementation("gg.aquatic:kregistry:26.0.54")
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

- Discord: [Aquatic Development](https://discord.com/invite/ffKAAQwNdC)
- Issues: open a ticket on GitHub for bugs or feature requests.
