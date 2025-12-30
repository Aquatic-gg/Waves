# KRegistry

[![CodeFactor](https://www.codefactor.io/repository/github/mrlarkyy/kregistry/badge)](https://www.codefactor.io/repository/github/mrlarkyy/kregistry)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/KRegistry?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/KRegistry)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A lightweight, type-safe registry system for Kotlin. KRegistry provides a structured way to manage object lifecycles through mutable and immutable (frozen) states, supporting both simple key-value lookups and complex hierarchical type-based registries.

## ✨ Features

*   **Immutability Patterns:** Transition between `MutableRegistry` (write) and `FrozenRegistry` (read-only) to guarantee thread safety.
*   **Atomic Global State:** A central `Registry` graph that uses atomic compare-and-swap operations for lock-free updates.
*   **Hierarchical Lookups:** Search for registered objects by their specific implementation class or by any inherited interface/parent class.
*   **Zero Boilerplate:** Utilizes Kotlin reified generics to provide a clean, type-safe API without manual casting.

---

## 📦 Installation

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    implementation("gg.aquatic:kregistry:25.0.1")
}
```

---

## 🚀 Getting Started

### Basic Usage

Create a registry, fill it with data, and freeze it to prevent accidental modification during runtime.

```kotlin
val mutable = MutableRegistry<String, String>()
mutable.register("api_key", "secret_value")

val frozen = mutable.freeze()
val key = frozen["api_key"] // Returns "secret_value"
```

### The Global Registry Graph

Use the global `Registry` object to manage multiple registries across your entire application.

```kotlin
val SERVICES = RegistryKey<String, BaseService>(RegistryId("services"))

// Atomic update (thread-safe)
Registry.update {
    val myReg = MutableRegistry<String, BaseService>()
    myReg.register("auth", AuthenticationService())
    
    registerRegistry(SERVICES, myReg.freeze())
}

// Accessing from anywhere
val auth = Registry[SERVICES]["auth"]
```

### Typed & Hierarchical Registries

`TypedRegistry` allows you to group objects by their Class type and perform powerful lookups.

```kotlin
// Retrieve an object by its exact implementation type
val provider = myTypedRegistry.getTyped<String, BaseService, MyImplementation>("provider_id")

// Retrieve all objects that implement a specific interface
val allServices = myTypedRegistry.getAllHierarchical<String, BaseService, IService>()
```

## 🛠️ Core Concepts

### Mutable vs. Frozen
- **`MutableRegistry`**: Used during initialization. Supports `register()`, `unregister()`, and `clear()`.
- **`FrozenRegistry`**: A read-only snapshot. Once a registry is frozen, its state is immutable, making it safe for sharing across your application. You can use `.unfreeze()` or `.updateRegistry { ... }` to modify a copy.

### The `TypedRegistry`
The project leverages a specific typealias to manage complex sets of data:
`typealias TypedRegistry<Id, Type> = FrozenRegistry<Class<*>, FrozenRegistry<Id, Type>>`


---

## 💬 Community & Support

Got questions, need help, or want to showcase what you've built with **KEvent**? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

*   **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
*   **Issues**: Open a ticket on GitHub for bugs or feature requests.
