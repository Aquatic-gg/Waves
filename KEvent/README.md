## KEvent

Lightweight library for EventBus

Features:
- Simple API
- Kotlin Based
- No Reflection
- Priority based subscriptions
- Cancellable events with ignoreCancel handling
- Thread safe
- Coroutines support
- Weak subscriptions
- Hierarchical lookups

## Example usage
````kotlin
val bus = eventBusBuilder {
    scope = null // Default - Uses runBlocking {} when null, so have this in mind.
    exceptionHandler = { e -> println(e) }
    hierarchical = true // Default
}

bus.subscribe<ExampleEvent> { e ->
    // Handle event
}


bus.subscribe<ExampleEvent>(ignoreCancelled = true) { e ->
    e.cancelled = true
}


bus.subscribe<ExampleEvent>(priority = EventPriority.HIGHEST, ignoreCancelled = true) { e ->
    // Handle event
}

bus.subscribeWeak<ExampleEvent> { e ->
    // Handle event
}

bus.post(ExampleEvent())

````

## Importing
Gradle kts
````kotlin
repositories {
    maven {
        url = uri("https://repo.nekroplex.com/releases")
    }
}
````

````kotlin
dependencies {
    implementation("gg.aquatic:KEvent:1.0.3")
}
````

Credits:

Huge thanks to [EventBus](https://github.com/Revxrsal/EventBus) by [Revxrsal](https://github.com/Revxrsal) for inspiration and API.