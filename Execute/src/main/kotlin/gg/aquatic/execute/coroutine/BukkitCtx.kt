package gg.aquatic.execute.coroutine

import gg.aquatic.execute.Execute
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

object BukkitCtx : SingleThreadCtx() {

    override val scope = CoroutineScope(
        this + SupervisorJob() + CoroutineExceptionHandler { _, e ->
            Execute.plugin.logger.severe("An error occurred while running a task!")
            e.printStackTrace()
        },
    )

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !Bukkit.getServer().isPrimaryThread
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (isDispatchNeeded(context)) {
            Bukkit.getScheduler().runTask(Execute.plugin, Runnable {
                try {
                    block.run()
                } catch (e: Throwable) {
                    Execute.plugin.logger.severe("An error occurred while running a task!")
                    e.printStackTrace()
                }
            })
        } else {
            block.run()
        }
    }

    override fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return scope.launch(block = block)
    }

    operator fun invoke(block: suspend CoroutineScope.() -> Unit) = launch(block = block)
    operator fun invoke() = scope
}