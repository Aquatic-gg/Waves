# Pakket 📦

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/pakket/badge)](https://www.codefactor.io/repository/github/mrlarkyy/pakket)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/Pakket?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/Pakket)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A lightweight, multi-module Kotlin abstraction layer for Minecraft Packet handling and NMS utilities. **Pakket** provides a version-independent API to interact with low-level server functions without the boilerplate.

## ✨ Key Features

- **Zero Initialization**: No `onEnable` hooks or manual configuration required.
- **KEvent Integration**: High-performance packet events powered by [KEvent](https://github.com/MrLarkyy/KEvent).
- **Multi-Module NMS**: Automatic version detection and implementation loading via the `NMSHandler`.
- **Packet Entities**: Simplified creation and management of non-server-side entities.

---

## 📦 Installation

Add the repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    implementation("gg.aquatic:Pakket:VERSION")
}
```

---

## 🚀 Usage

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

## 📂 Project Structure

- `API`: Version-independent interfaces, events, and `PacketEntity` logic.
- `NMS_1_21_9`: Specific implementation for Minecraft 1.21.9.
- `src`: Core entry point and version detection.


---

## 💬 Community & Support

Got questions, need help, or want to showcase what you've built with **Pakket**? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

*   **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
*   **Issues**: Open a ticket on GitHub for bugs or feature requests.