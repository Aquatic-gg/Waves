# KRegistry

A lightweight, type-safe registry system for Kotlin. KRegistry provides a structured way to manage object lifecycles through mutable and immutable (frozen) states, supporting both simple key-value lookups and complex hierarchical type-based registries.

## ✨ Features

*   **Immutable by Default:** Easily transition between `MutableRegistry` and `FrozenRegistry` to ensure data integrity.
*   **Typed Registries:** Built-in support for `TypedRegistry<Id, Type>`, allowing you to organize objects by their class type.
*   **Hierarchical Lookups:** Fetch entries not just by their specific class, but also via inherited interfaces or parent classes.
*   **Type Safety:** Heavy use of reified generics to eliminate manual casting and `Class<T>` boilerplate.

---

## 📦 Installation

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("gg.aquatic:kregistry:VERSION")
}
```

---

## 🚀 Getting Started

### Basic Registry Usage

For simple key-value management where you want to lock the state after initialization:

```kotlin
// Create and populate a mutable registry
val mutableReg = MutableRegistry<String, String>()
mutableReg.register("version", "1.0.0")

// Freeze it to prevent further modifications
val registry = mutableReg.freeze()

// Accessing values (returns null if missing)
val version = registry["version"]
```

### Typed Registries

`TypedRegistry` is a powerful pattern for storing different implementations under a common base type:

```kotlin
// Define a TypedRegistry (Mapped by Class -> Registry)
var myRegistry: TypedRegistry<String, BaseService> = MutableRegistry<Class<*>, FrozenRegistry<String, BaseService>>().freeze()

// Registering items using reified extensions
myRegistry = myRegistry.updateRegistry {
    register("auth-service", AuthenticationProvider()) 
}

// Retrieve by specific type
val auth = myRegistry.getTyped<String, BaseService, AuthenticationProvider>("auth-service")

// Retrieve hierarchically (will find implementations of the requested type)
val allProviders = myRegistry.getAllHierarchical<String, BaseService, BaseService>()
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
