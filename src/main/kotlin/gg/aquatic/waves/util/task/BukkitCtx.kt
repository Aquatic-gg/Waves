package gg.aquatic.waves.util.task

import gg.aquatic.waves.Waves
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

object BukkitCtx : CoroutineDispatcher() {

    val scope = CoroutineScope(
        this + SupervisorJob() + CoroutineExceptionHandler { _, e ->
            Waves.INSTANCE.logger.severe("An error occurred while running a task!")
            e.printStackTrace()
        },
    )

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !Bukkit.getServer().isPrimaryThread
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (isDispatchNeeded(context)) {
            Bukkit.getScheduler().runTask(Waves.INSTANCE, Runnable {
                try {
                    block.run()
                } catch (e: Throwable) {
                    Waves.INSTANCE.logger.severe("An error occurred while running a task!")
                    e.printStackTrace()
                }
            })
        } else {
            block.run()
        }
    }

    operator fun invoke(block: suspend CoroutineScope.() -> Unit) = scope.launch(block = block)
    operator fun invoke() = scope
}