# KHolograms

Lightweight Kotlin hologram engine for Paper that renders text and item display lines, supports per-player placeholders,
and keeps updates chunk-aware and efficient.

## Features

- Multi-line holograms with per-line filters and failover lines.
- Text, item, and animated line types via `LineFactory`.
- Per-player placeholder rendering with live updates.
- View distance checks and chunk-aware ticking.
- Simple config loading from YAML.

## Requirements

- Paper 1.21.11 (Display entities).
- Java 21 / Kotlin 2.3.

## Installation

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
    mavenCentral()
}

dependencies {
    implementation("gg.aquatic:KHolograms:26.0.1") // core
    // implementation("gg.aquatic:KHolograms-serialization:26.0.1") // optional YAML loading
}
```

## Modules

- `core`: runtime holograms, lines, and handlers.
- `serialization`: YAML loaders and line factories (optional).

## Quick Start

```kotlin
// onEnable
HologramHandler.initialize()

// Register built-in line factories once (serialization module).
LineFactoryDefaults.registerDefaults()

val settings = Hologram.Settings(
    lines = listOf(
        TextHologramLine.Settings(
            height = 0.25,
            text = "<gold>Welcome %player_name%</gold>",
            lineWidth = 120,
            scale = 1.0f,
            billboard = org.bukkit.entity.Display.Billboard.CENTER,
            conditions = listOf(),
            hasShadow = true,
            backgroundColor = null,
            isSeeThrough = true,
            transformationDuration = 0,
            failLine = null,
            teleportInterpolation = 0,
            translation = org.joml.Vector3f(0f, 0f, 0f),
        )
    ),
    conditions = listOf(),
    viewDistance = 48
)

val hologram = settings.create(
    location = someLocation,
    placeholderContext = { placeholderContext }
)
```

## Loading From YAML

```kotlin
LineFactoryDefaults.registerDefaults()

val holograms = HologramSerializer.loadFromFolder(dataFolder.resolve("holograms"))
for ((settings, locations) in holograms) {
    for (lazyLocation in locations) {
        val location = lazyLocation.toLocation() ?: continue
        settings.create(location) { placeholderContext }
    }
}
```

Example YAML (structure-focused; location format depends on `LazyLocation` in AquaticCommon):

```yaml
holograms:
  - view-distance: 64
    lines:
      - "Welcome to the hub"
      - type: text
        text: "<yellow>%player_name%</yellow>"
        line-width: 120
        has-shadow: true
      - type: item
        item:
          type: DIAMOND
          amount: 1
      - - 20: "Frame 1"
        - 20: "Frame 2"
    locations:
      - "world;0;64;0;0;0"
```

## Notes

- Call `HologramHandler.initialize()` once during plugin startup to register ticking and chunk listeners.
- Line visibility uses `view-conditions` and hologram visibility uses `view-requirements` (via Execute conditions).
- You can attach hologram lines as passengers with `setAsPassenger(seatEntityId)` to keep them mounted.

## Contributing

Pull requests are welcome. Please include a clear description of the change and the intended behavior.
