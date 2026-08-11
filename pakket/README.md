# Pakket

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/pakket/badge)](https://www.codefactor.io/repository/github/mrlarkyy/pakket)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/pakket?color=40c14a&name=Reposilite)](https://repo.aquatic.gg/#/releases/gg/aquatic/pakket)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A multi-module Kotlin abstraction layer for Minecraft packet handling and NMS utilities. Pakket provides a
version-independent API for low-level server functions.

## Key Features

- **No initialization**: No `onEnable` hooks or manual configuration required.
- **KEvent integration**: Packet events powered by [KEvent](https://github.com/MrLarkyy/KEvent).
- **Multi-module NMS**: Automatic version detection and implementation loading via the `NMSHandler`.
- **Packet entities**: Creation and management of non-server-side entities.

---

## Installation

Add the repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.aquatic.gg/releases")
}

dependencies {
    implementation("gg.aquatic:pakket:26.0.54")
}
```

---

## Usage

### Accessing the NMS Handler
Pakket uses a lazy-loaded instance to provide the correct NMS implementation for your server version.

```kotlin
val nmsHandler = Pakket.nmsHandler

// Example: Sending a packet
val packet = nmsHandler.createBlockChangePacket(location, blockData)
nmsHandler.sendPacket(packet, false, player)
```

### Listening to Events
Pakket fires specialized packet events through its internal `EventBus`. You can use the `packetEvent` helper for a cleaner syntax.

```kotlin
// Using the helper method
packetEvent<PacketBlockChangeEvent> { event ->
    println("${event.player.name} received a block change at ${event.x}, ${event.z}")
}

// Or subscribing via the EventBus directly
NMSHandler.eventBus.subscribe<PacketChunkLoadEvent> { event ->
    // Handle chunk load
}
```

---

## Project Structure

- `API`: Version-independent interfaces, events, and `PacketEntity` logic.
- `NMS_1_21_9`: Specific implementation for Minecraft 1.21.9.
- `src`: Core entry point and version detection.


---

## Community & Support

- Discord: [Aquatic Development](https://discord.com/invite/ffKAAQwNdC)
- Issues: open a ticket on GitHub for bugs or feature requests.