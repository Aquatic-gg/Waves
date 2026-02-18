# KRegistry

[![CodeFactor](https://www.codefactor.io/repository/github/mrlarkyy/kregistry/badge)](https://www.codefactor.io/repository/github/mrlarkyy/kregistry)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/KRegistry?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/KRegistry)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A lightweight, type-safe registry system for Kotlin. KRegistry provides a bootstrap-driven registry graph with hierarchical type lookups and typed collections.

## Features

*   **Bootstrap-Driven Registries:** Build a registry graph from contributions with deterministic initialization.
*   **Atomic Global State:** The registry graph uses compare-and-swap for safe concurrent updates.
*   **Hierarchical Lookups:** Search by specific implementation class or any inherited interface/parent class.
*   **Typed Collections:** Group instances by their runtime class using `GenericTyped<T>`.
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
val SERVICES = RegistryKey<String, Service>(RegistryId("core", "services"))

object AppBootstrap : BootstrapHolder
object CoreHolder : RegistryHolder

fun bootstrap() {
    // Register contributions
    CoreHolder.registryBootstrap(AppBootstrap) {
        registry(SERVICES) {
            add("auth", AuthService())
            add("db", DatabaseService())
        }
    }

    // Build registries
    val build = AppBootstrap.inject()
    build()
}

// Later...
val services = AppBootstrap[SERVICES]
val auth = services.get("auth")
```

### Typed & Hierarchical Registries

`TypedRegistry` allows you to group objects by their Class type and perform powerful lookups.

```kotlin
// Retrieve an object by its exact implementation type
val provider = myTypedRegistry.getTypedByClass("provider_id", MyImplementation::class.java)

// Retrieve all objects that implement a specific interface
val allServices = myTypedRegistry.getAllHierarchicalByClass(IService::class.java)
```

**Exact vs Hierarchical**
Exact lookups only return entries registered under the exact class key.
Hierarchical lookups include entries whose class is a subclass/implements the requested type.

### Typed Collections

Use `GenericTyped<T>` to group values by their runtime class and query them hierarchically.

```kotlin
interface Animal
class Dog : Animal

// A typed entry binds a value to its runtime type
data class AnimalEntry(
    override val value: Animal
) : ValueTyped<Animal>

val builder = RegistryContributionBuilder<Class<out Animal>, List<GenericTyped<out Animal>>>()
builder.addTyped(AnimalEntry(Dog()))

val typedCollection: TypedCollectionRegistry<Animal> = Registry(
    RegistryKey.typedCollection<Animal>(RegistryId("example", "animals")),
    builder.data,
    emptyMap()
)

val dogs = typedCollection.getTypedEntries<Dog>()
val allAnimals = typedCollection.getAllHierarchicalEntries<Animal>()
```

### Grouped Registries (Binder Pattern)

Use a grouped registry when your values are keyed by a binder type (e.g., `Action<Player>`).

```kotlin
interface Action<B>
class Player
data class SendMessage(val text: String) : Action<Player>

val ACTIONS = RegistryKey.grouped<String, Player, Action<Player>>(RegistryId("example", "actions"))
val PLAYER_ACTIONS = RegistryKey<String, Action<Player>>(RegistryId("example", "player-actions"))

val contribution: ContributionBuilder.() -> Unit = {
    registry(ACTIONS) {
        addGrouped(Player::class.java, PLAYER_ACTIONS) {
            add("message", SendMessage("hello"))
        }
    }
}

// Later...
val actions: GroupedRegistry<String, Player, Action<Player>> = AppBootstrap[ACTIONS]
val message = actions.getTypedByClass("message", Player::class.java)
```

### Contribution DSL

You can also pass a `ContributionBuilder.() -> Unit` around and apply it later:

```kotlin
val ANIMALS = RegistryKey.typedCollection<Animal>(RegistryId("example", "animals"))

val contribution: ContributionBuilder.() -> Unit = {
    registry(ANIMALS) {
        addTyped(AnimalEntry(Dog::class.java, Dog()))
    }
}
```

## Core Concepts

### The Registry Graph
The registry graph is managed internally. `BootstrapHolder` exposes helpers to build and access registries.

### The `TypedRegistry`
The project leverages a specific typealias to manage complex sets of data:
`typealias TypedRegistry<Id, Type> = Registry<Class<*>, Registry<Id, out Type>>`

---

## Community & Support

Got questions, need help, or want to showcase what you've built with KRegistry? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

*   **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
*   **Issues**: Open a ticket on GitHub for bugs or feature requests.
