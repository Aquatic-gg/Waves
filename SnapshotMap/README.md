# SnapshotMap

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/snapshotmap/badge)](https://www.codefactor.io/repository/github/mrlarkyy/snapshotmap)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/snapshotmap?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/snapshotmap)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A high-performance, read-optimized `MutableMap` wrapper for Kotlin/JVM.

`SnapshotMap` is designed for scenarios where map iterations (`forEach`) are frequent but modifications are occasional.
It uses an internal **Array-Snapshot** strategy to provide ultra-fast, lock-free iteration that significantly
outperforms standard `ConcurrentHashMap`.

## Features

- **Zero-Allocation Iteration:** Once the snapshot is cached, `forEach` performs no allocations and avoids `Map.Entry`
  overhead.
- **CPU Cache Friendly:** Data is stored in contiguous arrays, maximizing L3 cache hits during full-map scans.
- **Lock-Free Reads:** Standard point-lookups (`get`) delegate directly to the underlying map with zero-cost
  abstraction.
- **Smart Invalidation:** Snapshots are lazily rebuilt only when data actually changes, preventing redundant work during
  frequent "no-op" writes.
- **Coroutine Friendly:** Includes `SuspendingSnapshotMap` using `Mutex` for non-blocking iteration in asynchronous environments.

---

## When to use SnapshotMap

`SnapshotMap` is a specialized tool. It is not a 1:1 replacement for `ConcurrentHashMap` in every scenario.

### 💡 Which implementation to choose?

*   **`SnapshotMap`**: Use this for standard threaded applications. It uses `synchronized` for snapshot rebuilding, which is highly optimized by the JVM.
*   **`SuspendingSnapshotMap`**: Use this in **Kotlin Coroutines** (e.g., Ktor, Quarkus). It uses a `Mutex`, ensuring that threads are never blocked during a snapshot rebuild, keeping your event loop responsive.

### ✅ Use SnapshotMap when:

* **Read-Iteration Heavy:** Your application calls `forEach` significantly more often than it calls `put` or `remove`.
* **High Thread Contention:** Multiple threads are iterating over the map simultaneously. `SnapshotMap` eliminates the
  internal locking bottlenecks of standard concurrent collections.
* **Data Stability:** Your data changes in bursts or at set intervals (e.g., a game tick or a periodic config reload).
* **Large Map Scans:** You are iterating over thousands of items where the CPU cache benefit of a flat array becomes
  noticeable.

### ❌ Avoid SnapshotMap when:

* **Write-Heavy Workloads:** If you are modifying the map as frequently as you are reading it, the overhead of constant
  array rebuilding will make it slower than a standard `ConcurrentHashMap`.
* **Memory Constrained:** This map stores a cached copy of your keys and values in arrays, effectively doubling the
  reference memory usage of the map.

---

## Performance Benchmarks

In our JMH tests with 100,000 items, `SnapshotMap` demonstrates superior scalability in read-heavy environments.

### 1. Iteration Scalability (The "Win")

*Measured with 7 threads iterating and 1 thread performing occasional writes (100ms interval).*
Once the snapshot is cached, **SnapshotMap is ~2.5x faster** than `ConcurrentHashMap`. The `SuspendingSnapshotMap` performs nearly identically to the synchronous version thanks to `inline` optimization.

![Iteration Scalability](scalability_results.png)

### 2. Single-Threaded Baseline (vs HashMap)

In a single-threaded environment, `SnapshotMap` trades a small amount of raw lookup speed for a massive boost in scan
efficiency. It is **~50% faster in iteration** due to cache locality, but **~30% slower in point-reads** because of
delegation and thread-safety overhead.

|  Point-Read Performance (HashMap Wins)  |   Iteration Performance (SnapshotMap Wins)   |
|:---------------------------------------:|:--------------------------------------------:|
| ![Single Read](single_read_results.png) | ![Single Iteration](single_iter_results.png) |

### 3. Multi-Threaded Point R/W

*Standard point-lookups remain highly competitive with native `ConcurrentHashMap` performance.*

![Read/Write Contention](rw_results.png)

---

## Usage

### Installation

Add the library to your project:

````kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    implementation("gg.aquatic:snapshotmap:26.0.1")
}
````

### Basic Example (Blocking)

```kotlin
// Wraps any ConcurrentHashMap (defaults to a new one)
val map = SnapshotMap<String, Int>()

// High-Performance Iteration (Snapshot optimized)
map.forEach { key, value ->
    println("$key -> $value")
}
```

### Coroutine Example (Non-Blocking)

```kotlin
val map = SuspendingSnapshotMap<String, Int>()

// Inside a coroutine scope
suspend fun processData() {
    // Non-blocking iteration using Mutex
    map.forEachSuspended { key, value ->
        println("Processing $key")
    }
}
```

---

## 💬 Community & Support

Join our community for support or to discuss performance optimizations!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)
