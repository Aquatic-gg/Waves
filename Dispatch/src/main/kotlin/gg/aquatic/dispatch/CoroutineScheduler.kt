package gg.aquatic.dispatch

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.max

/**
 * CoroutineScheduler handles task scheduling and execution with support for adjustable scheduling
 * strategies and lifecycle management. Task scheduling is managed using coroutines and configurable
 * dispatchers, allowing precise control over task execution timing and concurrency.
 *
 * @constructor Creates a CoroutineScheduler with a specified scope and dispatcher.
 * @param scope The CoroutineScope used to launch the main scheduler coroutine. Defaults to a SupervisorJob-based scope.
 * @param dispatcher The CoroutineDispatcher for executing scheduler-related tasks. Defaults to a single-threaded dispatcher.
 *
 * @property events A read-only [Flow] that emits scheduler events such as task started, completed, or failed.
 * @property metrics A read-only [StateFlow] that provides real-time metrics about the scheduler's state, such as active tasks,
 * paused tasks, total executions, and failures.
 *
 * ### Core Methods:
 * - `runLater`: Schedules a task to run once after a specified delay.
 * - `runRepeatFixedDelay`: Schedules a recurring task with a fixed delay between executions.
 * - `runRepeatFixedRate`: Schedules a recurring task at a fixed rate.
 * - `pause`: Pauses a specific task by its ID.
 * - `resume`: Resumes a previously paused task.
 * - `cancel`: Cancels a scheduled task and removes it from the scheduler.
 * - `shutdown`: Shuts down the scheduler, cleaning up resources and stopping all executions.
 *
 * ### Task Scheduling:
 * Tasks can be scheduled with different strategies:
 * - **Once (`ONCE`)**: Executes a task once after a delay.
 * - **Fixed Delay (`FIXED_DELAY`)**: Executes a task repeatedly with a delay applied after the completion of each execution.
 * - **Fixed Rate (`FIXED_RATE`)**: Executes a task repeatedly at fixed intervals from the task start time, ensuring a consistent rate.
 *
 * ### Lifecycle:
 * - The scheduler runs a continuous loop, maintaining an internal task queue to manage task execution.
 * - Task states such as paused, cancelled, or completed are dynamically updated.
 * - Uses a message-driven architecture via [Channel] to process task-related commands like add, pause, resume, and cancel.
 *
 * ### Events and Metrics:
 * - Provides real-time feedback through [SchedulerEvent], such as task state changes.
 * - Monitors scheduler performance with [SchedulerMetrics], including the number of active, paused, and completed tasks, execution count, and failure count.
 *
 * ### Error Handling:
 * - Failed tasks generate a [SchedulerEvent.TaskFailed] event, and the failure count is incremented.
 * - Ensures that task failures do not disrupt the scheduler.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("unused")
open class CoroutineScheduler(
    scope: CoroutineScope = CoroutineScope(SupervisorJob()),
    val dispatcher: CoroutineDispatcher = Executors.newSingleThreadExecutor { r ->
        Thread(r, "SchedulerThread").apply { isDaemon = true }
    }.asCoroutineDispatcher()
) {
    private val logger = LoggerFactory.getLogger(CoroutineScheduler::class.java)

    private sealed class SchedulerMsg {
        data class Add(
            val task: Task,
            val firstRunMs: Long,
            val statusFlow: MutableStateFlow<ScheduledTask.Status>
        ) : SchedulerMsg()
        data class Pause(val id: TaskId) : SchedulerMsg()
        data class Resume(val id: TaskId) : SchedulerMsg()
        data class Cancel(val id: TaskId) : SchedulerMsg()
        object Shutdown : SchedulerMsg()
    }

    private val scope = CoroutineScope(scope.coroutineContext + dispatcher)

    private val msgChannel = Channel<SchedulerMsg>(capacity = Channel.UNLIMITED)

    private val _events = MutableSharedFlow<SchedulerEvent>(
        extraBufferCapacity = 64
    )
    val events: Flow<SchedulerEvent> = _events.asSharedFlow()

    private var executions = 0L
    private var failures = 0L

    private val _metrics = MutableStateFlow(
        SchedulerMetrics(0, 0, 0, 0, 0)
    )
    val metrics: StateFlow<SchedulerMetrics> = _metrics.asStateFlow()

    init {
        scope.launch {
            val tasks = mutableMapOf<TaskId, TaskState>()
            val runCounts = mutableMapOf<TaskId, Int>()
            val taskStatuses = mutableMapOf<TaskId, MutableStateFlow<ScheduledTask.Status>>()

            fun now() = System.currentTimeMillis()

            fun updateMetrics() {
                _metrics.value = SchedulerMetrics(
                    totalTasks = tasks.size,
                    activeTasks = tasks.values.count { !it.paused },
                    pausedTasks = tasks.values.count { it.paused },
                    executions = executions,
                    failures = failures
                )
            }

            suspend fun runTask(state: TaskState) {
                val start = now()
                taskStatuses[state.task.id]?.value = ScheduledTask.Status.RUNNING
                _events.tryEmit(SchedulerEvent.TaskStarted(state.task.id))

                try {
                    state.task.action()
                    executions++

                    val currentCount = (runCounts[state.task.id] ?: 0) + 1
                    runCounts[state.task.id] = currentCount

                    if (state.task.maxRepeats != null && currentCount >= state.task.maxRepeats) {
                        state.cancelled = true
                        taskStatuses[state.task.id]?.value = ScheduledTask.Status.FINISHED
                    } else {
                        taskStatuses[state.task.id]?.value = ScheduledTask.Status.SCHEDULED
                    }

                    _events.tryEmit(SchedulerEvent.TaskCompleted(state.task.id, now() - start))
                } catch (t: Throwable) {
                    failures++
                    logger.error("Task ${state.task.id} failed with an unhandled exception", t)
                    _events.tryEmit(SchedulerEvent.TaskFailed(state.task.id, t))
                }

                // Updates next run time based on schedule type
                when (state.task.type) {
                    ScheduleType.ONCE -> {
                        state.cancelled = true
                        taskStatuses[state.task.id]?.value = ScheduledTask.Status.FINISHED
                    }
                    ScheduleType.FIXED_DELAY -> state.nextRunTime = now() + state.task.intervalMs
                    ScheduleType.FIXED_RATE -> {
                        state.nextRunTime += state.task.intervalMs
                        if (state.nextRunTime < now()) {
                            state.nextRunTime = now() + state.task.intervalMs
                        }
                    }
                }
            }

            try {
                while (isActive) {
                    updateMetrics()

                    val nextTask = tasks.values
                        .filter { !it.cancelled && !it.paused }
                        .minByOrNull { it.nextRunTime }

                    val delayMs = nextTask?.let { max(0, it.nextRunTime - now()) }

                    select<Unit> {
                        // Waits until next task is due for execution
                        if (delayMs != null) {
                            onTimeout(delayMs) {
                                if (!nextTask.cancelled && !nextTask.paused) {
                                    runTask(nextTask)
                                    if (nextTask.cancelled) tasks.remove(nextTask.task.id)
                                }
                            }
                        }

                        msgChannel.onReceive { msg ->
                            when (msg) {
                                is SchedulerMsg.Add -> {
                                    tasks[msg.task.id] = TaskState(msg.task, msg.firstRunMs)
                                }
                                is SchedulerMsg.Pause -> {
                                    tasks[msg.id]?.paused = true
                                    taskStatuses[msg.id]?.value = ScheduledTask.Status.PAUSED
                                }
                                is SchedulerMsg.Resume -> tasks[msg.id]?.let {
                                    it.paused = false
                                    it.nextRunTime = now() + it.task.intervalMs
                                    taskStatuses[msg.id]?.value = ScheduledTask.Status.SCHEDULED
                                }
                                is SchedulerMsg.Cancel -> {
                                    tasks.remove(msg.id)
                                    runCounts.remove(msg.id)
                                    taskStatuses.remove(msg.id)?.value = ScheduledTask.Status.CANCELLED
                                }
                                SchedulerMsg.Shutdown -> {
                                    logger.info("Scheduler is shutting down...")
                                    tasks.clear()
                                    runCounts.clear()
                                    taskStatuses.values.forEach { it.value = ScheduledTask.Status.CANCELLED }
                                    taskStatuses.clear()
                                    msgChannel.close()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    logger.error("Fatal error in scheduler loop", e)
                }
            }
        }
    }

    private fun addTask(
        type: ScheduleType,
        intervalMs: Long,
        initialDelayMs: Long = intervalMs,
        maxRepeats: Int? = null,
        block: suspend () -> Unit
    ): ScheduledTask {
        val id = TaskId(UUID.randomUUID())
        val statusFlow = MutableStateFlow(ScheduledTask.Status.SCHEDULED)
        val task = Task(id, type, intervalMs, initialDelayMs, maxRepeats, block)
        msgChannel.trySend(SchedulerMsg.Add(task, System.currentTimeMillis() + initialDelayMs, statusFlow)).getOrThrow()
        return ScheduledTask(this, id, statusFlow.asStateFlow())
    }

    fun runLater(delayMs: Long, block: suspend () -> Unit): ScheduledTask =
        addTask(ScheduleType.ONCE, delayMs, delayMs, 1, block)

    fun runRepeatFixedDelay(
        intervalMs: Long,
        initialDelayMs: Long = intervalMs,
        repeats: Int? = null,
        block: suspend () -> Unit
    ): ScheduledTask = addTask(ScheduleType.FIXED_DELAY, intervalMs, initialDelayMs, repeats, block)

    fun runRepeatFixedRate(
        intervalMs: Long,
        initialDelayMs: Long = intervalMs,
        repeats: Int? = null,
        block: suspend () -> Unit
    ): ScheduledTask = addTask(ScheduleType.FIXED_RATE, intervalMs, initialDelayMs, repeats, block)

    fun pause(id: TaskId) = msgChannel.trySend(SchedulerMsg.Pause(id))
    fun resume(id: TaskId) = msgChannel.trySend(SchedulerMsg.Resume(id))
    fun cancel(id: TaskId) = msgChannel.trySend(SchedulerMsg.Cancel(id))

    fun shutdown() {
        msgChannel.trySend(SchedulerMsg.Shutdown)
        scope.cancel()
        if (dispatcher is CloseableCoroutineDispatcher) {
            dispatcher.close()
        }
    }
}