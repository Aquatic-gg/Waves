# KEvent

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/kevent/badge)](https://www.codefactor.io/repository/github/mrlarkyy/kevent)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/KEvent?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/KEvent)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)

**KEvent** is a high-performance, lightweight EventBus library for Kotlin. It is designed to be reflection-free, thread-safe, and deeply integrated with Kotlin Coroutines.

## ✨ Features

*   **No Reflection**: Extremely fast execution by avoiding expensive reflection lookups.
*   **Simple DSL**: Intuitive API for building the bus and registering listeners.
*   **Priority System**: Fine-grained control over listener execution order (from `HIGHEST` to `MONITOR`).
*   **Weak Subscriptions**: Prevent memory leaks by allowing listeners to be garbage collected automatically.
*   **Hierarchical Lookups**: Optionally handle events based on class inheritance.
*   **Coroutine Support**: Native support for `suspend` functions and async posting.
*   **Execution Metrics**: Track how long each listener takes to process an event.
*   **Cancellable Events**: Built-in support for event cancellation logic.

---

## 📦 Installation

Add the repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    implementation("gg.aquatic:KEvent:1.0.4")
}
```

## 🚀 Quick Start
### 1. Create the Event Bus
````kotlin
val bus = eventBusBuilder {
    scope = null // Uses runBlocking { } for sync posts. Set a CoroutineScope for async support.
    exceptionHandler = { sub, event, ex -> println("Error in ${sub.name}: ${ex.message}") }
    hierarchical = true // Match event subclasses (default: true)
}
````

### 2. Define Events
````kotlin
class UserLoginEvent(val username: String)

class CancellableAction(override var cancelled: Boolean = false) : Cancellable
````

### 3. Subscribe to Events
````kotlin
// Basic subscription
bus.subscribe<UserLoginEvent> { event ->
    println("Welcome, ${event.username}!")
}

// Priority & Cancellation handling
bus.subscribe<CancellableAction>(
    priority = EventPriority.HIGHEST,
    ignoreCancelled = false // Won't run if the event was already cancelled
) { event ->
    // Handle logic...
}

// Weak subscription (Prevent memory leaks in temporary objects)
bus.subscribeWeak<UserLoginEvent> { println("Checking login...") }
````

### 4. Post Events
````kotlin
// Blocking post (returns Deferred)
bus.post(UserLoginEvent("Aquatic"))

// Suspend post (preferred inside coroutines)
val result = bus.postSuspend(UserLoginEvent("Aquatic"))

// Check metrics
result.executionTimes.forEach { (sub, time) ->
    println("Listener ${sub.name} took ${time}ms")
}
````

## 🤝 Credits

Inspired by [EventBus](https://github.com/Revxrsal/EventBus) by [Revxrsal](https://github.com/Revxrsal).