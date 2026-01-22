# Dispatch

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/dispatch/badge)](https://www.codefactor.io/repository/github/mrlarkyy/dispatch)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/Dispatch?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/Dispatch)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

Dispatch is a lightweight, coroutine-based task scheduler library for Kotlin, designed to handle asynchronous task
execution with support for one-time delays, fixed-delay repetitions, and fixed-rate repetitions. It provides robust task
management features, including pausing, resuming, and canceling tasks via returned task objects, along with real-time
metrics and event tracking.

## Features

- **Flexible Scheduling**: Schedule tasks to run once after a delay, or repeatedly at fixed intervals (delay or
  rate-based). Supports **limited repeats** and **delayed starts**.
- **Coroutine-Powered**: Built on Kotlin coroutines for efficient, non-blocking execution.
- **Task Management**: Pause, resume, or cancel tasks directly on returned task objects. Includes a **built-in
  status flow** to track a task's lifecycle.
- **Customizable Execution Context**: Specify your own coroutine scope and dispatcher for integration with frameworks
  like BukkitScheduler.
- **Metrics and Monitoring**: Access live statistics on task counts, executions, and failures.
- **Event Streaming**: Subscribe to a flow of events for task lifecycle notifications (start, completion, failure).
- **Thread Safety**: Operates on a specified dispatcher to ensure controlled task handling.
- **Lifecycle Control**: Integrates seamlessly with coroutine scopes for proper cancellation and error handling.

---

## Installation

Add the following dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}
```

```kotlin
dependencies {
    implementation("gg.aquatic.dispatch:dispatch:26.0.1")
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

## 💬 Community & Support

Got questions, need help, or want to showcase what you've built with **Dispatch**? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

* **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
* **Issues**: Open a ticket on GitHub for bugs or feature requests.