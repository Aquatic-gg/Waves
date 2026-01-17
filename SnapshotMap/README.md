# ShardedMap

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/shardedmap/badge)](https://www.codefactor.io/repository/github/mrlarkyy/shardedmap)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/shardedmap?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/shardedmap)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A high-performance, thread-safe sharded map implementation for Kotlin/JVM. 

`ShardedMap` reduces lock contention by splitting the dataset into multiple independent segments (shards), each with its own lock. This allows multiple threads to perform operations on different parts of the map simultaneously.

## Features

- **Low Contention:** Optimized for multi-threaded environments where a single global lock would be a bottleneck.
- **Cache Optimized:** Internal segments use manual padding to prevent "False Sharing" on CPU cache lines.
- **No-Allocation Iteration:** `forEach` provides weakly-consistent iteration across all shards without creating snapshots or temporary objects.
- **Power-of-Two Sharding:** Uses bitwise masking for ultra-fast shard lookup.

## Performance Benchmarks

Below are the throughput results (Operations per Second) comparing `ShardedMap` (256 shards) against Java's `ConcurrentHashMap`.

### 1. Read/Write Contention
*Measured with 4 threads reading and 1 thread writing simultaneously.*

![Read/Write Contention](rw_results.png)

### 2. Iteration Performance
*Measured by calculating the sum of values across the entire map.*

![Iteration Performance](iteration_results.png)

## Usage

### Installation
Add the library to your project (Update with your specific coordinates):
```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    implementation("gg.aquatic:shardedmap:26.0.1")
}
```

### Basic Example
```kotlin
val map = ShardedMap<String, Int>()

// Set and Get
map["Apple"] = 10
val count = map["Apple"]

// Thread-safe iteration
map.forEach { key, value ->
    println("$key -> $value")
}

// Remove
map -= "Apple"
```

---

## 💬 Community & Support

Got questions, need help, or want to showcase what you've built with **ShardedMap**? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

*   **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
*   **Issues**: Open a ticket on GitHub for bugs or feature requests.