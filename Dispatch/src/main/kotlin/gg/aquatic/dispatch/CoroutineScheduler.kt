package gg.aquatic.dispatch

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.selects.onTimeout
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.max

/**
 * A class responsible for scheduling and executing coroutines based on fixed intervals, delays, or rates.
 * This scheduler provides task management functionality, allowing tasks to be paused, resumed, or canceled.
 *
 * @constructor Creates an instance of CoroutineScheduler with an optional parent CoroutineScope.
 * The scheduler operates on a dedicated single-threaded executor for task execution.
 *
 * @param parentScope The parent [CoroutineScope] to inherit coroutine context from. Defaults to a [SupervisorJob] scope.
 *
 * The scheduler supports the following scheduling types:
 * - Execute a task once after a specified delay.
 * - Repeatedly execute a task at fixed intervals, starting after the initial execution's completion.
 * - Repeatedly execute a task at fixed rates, adhering to a fixed time between task starts.
 *
 * Core Features:
 * - Task Execution: Supports single and recurring task execution of coroutines.
 * - Metrics Collection: Provides live metrics such as total tasks, active tasks, paused tasks,
 *   completed executions, and failures.
 * - Event Emission: Offers a [Flow] for tracking task lifecycle events (start, completion, failure).
 * - Task Management: Allows runtime suspension, resumption, or cancellation of tasks.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("unused")
class CoroutineScheduler(
    parentScope: CoroutineScope = CoroutineScope(SupervisorJob())
) {
    private val logger = LoggerFactory.getLogger(CoroutineScheduler::class.java)

    private sealed class SchedulerMsg {
        data class Add(val task: Task, val firstRunMs: Long) : SchedulerMsg()
        data class Pause(val id: TaskId) : SchedulerMsg()
        data class Resume(val id: TaskId) : SchedulerMsg()
        data class Cancel(val id: TaskId) : SchedulerMsg()
        object Shutdown : SchedulerMsg()
    }

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "SchedulerThread").apply { isDaemon = true }
    }
    private val dispatcher = executor.asCoroutineDispatcher()
    private val scope = CoroutineScope(parentScope.coroutineContext + dispatcher)

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
                _events.tryEmit(SchedulerEvent.TaskStarted(state.task.id))

                try {
                    state.task.action()
                    executions++
                    _events.tryEmit(SchedulerEvent.TaskCompleted(state.task.id, now() - start))
                } catch (t: Throwable) {
                    failures++
                    logger.error("Task ${state.task.id} failed with an unhandled exception", t)
                    _events.tryEmit(SchedulerEvent.TaskFailed(state.task.id, t))
                }

                // Updates next run time based on schedule type
                when (state.task.type) {
                    ScheduleType.ONCE -> state.cancelled = true
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
                                is SchedulerMsg.Pause -> tasks[msg.id]?.paused = true
                                is SchedulerMsg.Resume -> tasks[msg.id]?.let {
                                    it.paused = false
                                    it.nextRunTime = now() + it.task.intervalMs
                                }
                                is SchedulerMsg.Cancel -> tasks.remove(msg.id)
                                SchedulerMsg.Shutdown -> {
                                    logger.info("Scheduler is shutting down...")
                                    tasks.clear()
                                    msgChannel.close()
                                    scope.cancel()
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
        block: suspend () -> Unit
    ): TaskId {
        val id = UUID.randomUUID()
        val task = Task(id, type, intervalMs, block)
        msgChannel.trySend(SchedulerMsg.Add(task, System.currentTimeMillis() + intervalMs))
        return id
    }

    fun runLater(delayMs: Long, block: suspend () -> Unit): TaskId =
        addTask(ScheduleType.ONCE, delayMs, block)

    fun runRepeatFixedDelay(intervalMs: Long, block: suspend () -> Unit): TaskId =
        addTask(ScheduleType.FIXED_DELAY, intervalMs, block)

    fun runRepeatFixedRate(intervalMs: Long, block: suspend () -> Unit): TaskId =
        addTask(ScheduleType.FIXED_RATE, intervalMs, block)

    fun pause(id: TaskId) = msgChannel.trySend(SchedulerMsg.Pause(id))
    fun resume(id: TaskId) = msgChannel.trySend(SchedulerMsg.Resume(id))
    fun cancel(id: TaskId) = msgChannel.trySend(SchedulerMsg.Cancel(id))

    fun shutdown() {
        msgChannel.trySend(SchedulerMsg.Shutdown)
        scope.cancel()
        dispatcher.close()
        executor.shutdown()
    }
}