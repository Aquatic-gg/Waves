# KEvent

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/kevent/badge)](https://www.codefactor.io/repository/github/mrlarkyy/kevent)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/kevent?color=40c14a&name=Reposilite)](https://repo.aquatic.gg/#/releases/gg/aquatic/kevent)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

KEvent is a reflection-free event bus for Kotlin, built around Kotlin coroutines and safe for concurrent use.

## Features

*   **No reflection**: Listener dispatch avoids reflection lookups.
*   **DSL**: API for building the bus and registering listeners.
*   **Priorities**: Control listener execution order, from `HIGHEST` to `MONITOR`.
*   **Weak subscriptions**: Allow listeners to be garbage collected to avoid memory leaks.
*   **Hierarchical lookups**: Optionally match events by class inheritance.
*   **Coroutines**: Supports `suspend` functions and async posting.
*   **Metrics**: Track how long each listener takes to process an event.
*   **Cancellable events**: Built-in support for event cancellation.

---

## Installation

Add the repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.aquatic.gg/releases")
}

dependencies {
    implementation("gg.aquatic:kevent:26.0.54")
}
```

---

## Quick Start
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

---

## Best Practices & Performance

### Coroutine Scope
When building the `EventBus`, if you leave `scope = null`, the bus uses `runBlocking { }` for synchronous calls.
> **Warning:** `runBlocking` blocks the current thread until all listeners finish. For high-concurrency environments,
> provide a `CoroutineScope` instead.

### Use `postSuspend`
Inside a suspending function or coroutine, prefer `bus.postSuspend(event)`. `bus.post(event)` returns a `Deferred` and
carries extra job-management overhead; `postSuspend` is a direct call that integrates with your existing coroutine
context.

### Hierarchical Lookups
If you have many subscriptions and never need inheritance matching (you only post exact classes), setting
`hierarchical = false` in the builder skips `isAssignableFrom` checks.

### Priority Management
Use `EventPriority.MONITOR` for listeners that only observe the final state of an event without modifying it.

---

## Community & Support

- Discord: [Aquatic Development](https://discord.com/invite/ffKAAQwNdC)
- Issues: open a ticket on GitHub for bugs or feature requests.

---

## Credits

Inspired by [EventBus](https://github.com/Revxrsal/EventBus) by [Revxrsal](https://github.com/Revxrsal).