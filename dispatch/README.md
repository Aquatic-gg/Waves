# Dispatch

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/dispatch/badge)](https://www.codefactor.io/repository/github/mrlarkyy/dispatch)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/dispatch?color=40c14a&name=Reposilite)](https://repo.aquatic.gg/#/releases/gg/aquatic/dispatch)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

Dispatch is a coroutine-based task scheduler for Kotlin. It handles one-time delays, fixed-delay repetitions, and
fixed-rate repetitions, and lets you pause, resume, and cancel tasks through returned task objects. It also exposes
metrics and a lifecycle event stream.

## Features

- **Scheduling**: Run tasks once after a delay, or repeatedly at fixed intervals (delay or rate-based). Supports limited
  repeats and delayed starts.
- **Coroutine-based**: Built on Kotlin coroutines for non-blocking execution.
- **Task management**: Pause, resume, or cancel tasks directly on returned task objects, with a status flow to track a
  task's lifecycle.
- **Execution context**: Specify your own coroutine scope and dispatcher, e.g. to integrate with BukkitScheduler.
- **Metrics**: Live statistics on task counts, executions, and failures.
- **Events**: Subscribe to a flow of task lifecycle notifications (start, completion, failure).
- **Lifecycle control**: Integrates with coroutine scopes for cancellation and error handling.

---

## Installation

Add the following dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.aquatic.gg/releases")
}
```

```kotlin
dependencies {
    implementation("gg.aquatic:dispatch:26.0.54")
}
```

---

## Quick Start

Here's a basic example of how to use Dispatch:

```kotlin
fun main() {
    val scheduler = CoroutineScheduler()

    // Schedule a task to run once after 1 second
    val task = scheduler.runLater(1000L) {
        println("Hello, world!")
    }

    // You can manage the task directly
    // task.pause()  // If needed
    // task.cancel() // To stop it

    // Keep the main thread alive for demonstration
    Thread.sleep(2000)
    scheduler.shutdown()
}
```

---

## Usage Guide

### Creating a Scheduler

```kotlin
val scheduler = CoroutineScheduler()
```

You can customize the coroutine scope and dispatcher:

```kotlin
val customScope = CoroutineScope(SupervisorJob())
val customDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
val scheduler = CoroutineScheduler(scope = customScope, dispatcher = customDispatcher)
```

#### Why Specify Scope and Dispatcher?

- **Scope**: Controls the lifecycle and context of the scheduler's coroutines. Specifying a custom scope allows
  inheritance of job hierarchies, exception handlers, and other context elements. This is essential for integrating with
  larger applications, ensuring coordinated shutdown, and propagating errors. Defaults to a new scope with
  `SupervisorJob`.
- **Dispatcher**: Determines the thread or thread pool where tasks execute. Use a custom dispatcher to integrate with
  specific execution contexts, such as the main thread in frameworks like Minecraft (Bukkit). Defaults to a
  single-threaded dispatcher for sequential, safe execution. If providing a custom dispatcher, manage its lifecycle
  externally, as the scheduler will attempt to close it on shutdown if possible.

### Scheduling Tasks

Dispatch supports three scheduling types, each returning a `ScheduledTask` for direct management:

1. **Once (Delayed Execution)**: Run a task once after a specified delay.
2. **Fixed Delay**: Run a task repeatedly, waiting for the previous execution to complete before starting the next.
3. **Fixed Rate**: Run a task repeatedly at fixed time intervals, regardless of execution time.

#### Examples

```kotlin
// Run once after 2 seconds
val onceTask = scheduler.runLater(2000L) {
    println("This runs once")
}
onceTask.cancel() // Cancel if needed

// Run every 1 second, fixed delay (waits for completion)
val delayTask = scheduler.runRepeatFixedDelay(1000L) {
    println("Fixed delay task")
    delay(500) // Simulates work
}
delayTask.pause()  // Pause
delayTask.resume() // Resume later

// Run every 1 second, fixed rate (strict timing)
val rateTask = scheduler.runRepeatFixedRate(1000L) {
    println("Fixed rate task")
    delay(500) // If this takes time, next run may start late
}
rateTask.cancel() // Stop permanently
```

#### Advanced Scheduling Options

You can specify an `initialDelayMs` and a limit on `repeats`:

```kotlin
// Start after 5 seconds, repeat every 1 second, but only 10 times total
val limitedTask = scheduler.runRepeatFixedDelay(
    intervalMs = 1000L, 
    initialDelayMs = 5000L, // Optional - uses intervalMs if not specified
    repeats = 10 // Optional - runs forever if not specified
) {
    println("I will only run 10 times!")
}
```

#### Task Status Tracking

Every `ScheduledTask` exposes a `status` as a `StateFlow`, allowing you to react to its lifecycle:

```kotlin
val task = scheduler.runLater(2000L) { /* ... */ }

// Check status directly
if (task.isFinished) {
    println("Task is no longer active")
}

// Or collect status changes reactively
scope.launch {
    task.status.collect { status ->
        when (status) {
            ScheduledTask.Status.SCHEDULED -> println("Waiting...")
            ScheduledTask.Status.RUNNING   -> println("Executing...")
            ScheduledTask.Status.PAUSED    -> println("On hold")
            ScheduledTask.Status.FINISHED  -> println("Done!")
            ScheduledTask.Status.CANCELLED -> println("Stopped")
        }
    }
}
```

#### Key Differences Between Fixed Delay and Fixed Rate

- **Fixed Delay**: The interval is measured from the end of one execution to the start of the next. If a task takes
  500ms and the interval is 1000ms, the next task starts 1000ms after completion (total cycle: 1500ms).
- **Fixed Rate**: The interval is measured from start to start. If a task takes 500ms and the interval is 1000ms, the
  next task starts 1000ms after the previous start, even if the first hasn't finished. If the task overruns, the next
  run may be skipped or delayed to catch up.

Use fixed delay for tasks where completion order matters, and fixed rate for time-sensitive tasks like heartbeats or
polls.

### Metrics and Events

#### Metrics

Access real-time metrics via the `metrics` StateFlow:

```kotlin
scheduler.metrics.collect { metrics ->
    println("Total tasks: ${metrics.totalTasks}")
    println("Active tasks: ${metrics.activeTasks}")
    println("Paused tasks: ${metrics.pausedTasks}")
    println("Executions: ${metrics.executions}")
    println("Failures: ${metrics.failures}")
}
```

Metrics include counts of total, active, and paused tasks, plus cumulative executions and failures.

#### Events

Subscribe to task events via the `events` Flow:

```kotlin
scheduler.events.collect { event ->
    when (event) {
        is SchedulerEvent.TaskStarted -> println("Task ${event.taskId} started")
        is SchedulerEvent.TaskCompleted -> println("Task ${event.taskId} completed in ${event.durationMs}ms")
        is SchedulerEvent.TaskFailed -> println("Task ${event.taskId} failed: ${event.throwable}")
    }
}
```

Events notify you of task starts, completions, and failures, useful for logging, monitoring, or triggering actions.

### Shutdown

Always shut down the scheduler to free resources:

```kotlin
scheduler.shutdown()
```

This cancels all tasks, closes channels, and shuts down the dispatcher if possible. After shutdown, the scheduler cannot
be reused.

---

## Platform Support (Paper/Bukkit)

Dispatch includes a module for Paper/Bukkit servers. It runs tasks on the server's primary thread or specific region
threads (for Folia compatibility) while keeping coroutine scheduling.

- **Thread switching**: Handles thread-switching and joins tasks, so the scheduler waits for the main thread to finish
  before proceeding.
- **Folia support**: Includes `BukkitCtx.OfLocation` and `BukkitCtx.OfEntity` for regional scheduling.
- **Non-blocking**: The scheduler runs in the background; only your task's action blocks the primary thread.

---

## Installation

Add the following dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    // Core module
    implementation("gg.aquatic:dispatch:26.0.54")
    implementation("gg.aquatic:dispatch-paper:26.0.54")
}
  ```
---

## Quick Start

To run tasks on the Bukkit Main Thread, provide a dispatcher that uses `BukkitCtx`. This ensures your code can safely access the Bukkit API.

```kotlin
val syncCtx = BukkitCtx.Global(myPlugin)
val scheduler = CoroutineScheduler(baseCtx = syncDispatcher)

// This task now runs safely on the Bukkit Main Thread!
scheduler.runRepeatFixedRate(20 * 50L) {
    Bukkit.broadcastMessage("Sync Tick!")
}
```

---

## Best Practices

- **Exception Handling**: Tasks should handle their own exceptions. Unhandled exceptions are logged and counted as
  failures but don't crash the scheduler.
- **Long-Running Tasks**: For fixed-rate tasks, ensure execution time doesn't exceed the interval to avoid skips.
  Consider fixed-delay for variable workloads.
- **Resource Management**: Use `shutdown()` in your application's cleanup logic (e.g., in a `finally` block or shutdown
  hook). Manage custom dispatchers' lifecycles externally.
- **Testing**: Use custom scopes and dispatchers with test utilities for deterministic testing.
- **Performance**: The default dispatcher uses a single thread; for CPU-intensive tasks, provide a multi-threaded
  dispatcher.

## Community & Support

- Discord: [Aquatic Development](https://discord.com/invite/ffKAAQwNdC)
- Issues: open a ticket on GitHub for bugs or feature requests.