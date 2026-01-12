package gg.aquatic.common.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * VirtualsCtx is an implementation of [CoroutineDispatcher] designed for managing coroutines
 * using virtual threads. It provides a coroutine context and allows dispatching, launching, and
 * executing tasks in a highly concurrent and lightweight virtual-thread-based environment.
 *
 * Features:
 * - Uses a virtual-thread-per-task executor for executing tasks.
 * - Provides a [CoroutineScope] with a [SupervisorJob] and a [CoroutineExceptionHandler] for
 *   managing coroutines and handling exceptions.
 * - Supports launching coroutines via function calls or operator invocation.
 * - Enables dispatching tasks to the underlying virtual thread executor.
 * - Provides utility methods for posting tasks and shutting down the executor.
 *
 * Functionalities:
 * - `dispatch`: Dispatches a task to the virtual thread executor.
 * - `isDispatchNeeded`: Always returns true, indicating that dispatching is required.
 * - `invoke` operator: Supports invoking a coroutine block or retrieving the [CoroutineScope].
 * - `launch`: Launches a coroutine within the context's scope.
 * - `post`: Submits a task to be executed by the virtual thread executor.
 * - `shutdown`: Gracefully shuts down the virtual thread executor.
 *
 * This class is suitable for applications that require high concurrency and lightweight threading
 * using Kotlin coroutines.
 */
object VirtualsCtx: CoroutineDispatcher() {

    private val logger: Logger = LoggerFactory.getLogger("Aquatic-Virtual-Thread")

    private val executor = Executors.newVirtualThreadPerTaskExecutor()

    val scope = CoroutineScope(
        this + SupervisorJob() + CoroutineExceptionHandler { _, e ->
            logger.error("Coroutine exception in VirtualsCtx")
            e.printStackTrace()
        }
    )

    override fun isDispatchNeeded(context: CoroutineContext): Boolean = true

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        executor.execute(block)
    }

    operator fun invoke(block: suspend CoroutineScope.() -> Unit) = launch(block = block)
    operator fun invoke() = scope

    fun launch(block: suspend CoroutineScope.() -> Unit) = scope.launch(block = block)

    fun post(task: () -> Unit) {
        executor.execute(task)
    }

    fun shutdown() {
        executor.shutdown()
    }

}