package gg.aquatic.dispatch

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

open class BaseCtx(
    val check: () -> Boolean,
    val execution: (Runnable) -> Unit,
    val logger: Logger
) : CoroutineDispatcher() {

    val scope = CoroutineScope(
        this + SupervisorJob() + CoroutineExceptionHandler { _, e ->
            logger.severe("An error occurred while running a task!")
            e.printStackTrace()
        },
    )

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !check()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (isDispatchNeeded(context)) {
            execution(block)
        } else {
            block.run()
        }
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return scope.launch(block = block)
    }

    operator fun invoke(block: suspend CoroutineScope.() -> Unit) = launch(block = block)
    operator fun invoke() = scope
}