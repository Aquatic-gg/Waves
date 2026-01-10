package gg.aquatic.kmenu.coroutine

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class SingleThreadedContext(name: String) : SingleThreadCtx() {

    private val logger: Logger = LoggerFactory.getLogger("Aquatic-Thread-$name")

    // Single worker thread dedicated to cache operations
    private val executor = Executors.newSingleThreadExecutor(
        Thread.ofPlatform()
            .name(name, 0)
            .daemon(true)
            .uncaughtExceptionHandler { t, e ->
                logger.error("Unhandled exception on $t in CacheCtx")
                e.printStackTrace()
            }
            .factory()
    )

    override val scope = CoroutineScope(
        this + SupervisorJob() + CoroutineExceptionHandler { _, e ->
            logger.error("Coroutine exception in CacheCtx")
            e.printStackTrace()
        }
    )

    override fun isDispatchNeeded(context: CoroutineContext): Boolean = true

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        executor.execute(block)
    }

    // Public helpers

    override fun launch(block: suspend CoroutineScope.() -> Unit) = scope.launch(block = block)

    fun post(task: () -> Unit) {
        executor.execute(task)
    }

    fun shutdown() {
        executor.shutdown()
    }
}