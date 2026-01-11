package gg.aquatic.common.coroutine

import gg.aquatic.common.AquaticCommon
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import kotlin.coroutines.CoroutineContext

class BukkitCtx(
    val check: () -> Boolean,
    val execution: (Runnable) -> Unit
) : SingleThreadCtx() {

    override val scope = CoroutineScope(
        this + SupervisorJob() + CoroutineExceptionHandler { _, e ->
            AquaticCommon.plugin.logger.severe("An error occurred while running a task!")
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

    override fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return scope.launch(block = block)
    }

    operator fun invoke(block: suspend CoroutineScope.() -> Unit) = launch(block = block)
    operator fun invoke() = scope

    companion object {
        val GLOBAL by lazy {
            BukkitCtx({ Bukkit.getServer().isGlobalTickThread }, { runnable ->
                Bukkit.getGlobalRegionScheduler().run(AquaticCommon.plugin) { runnable.run() }
            })
        }

        fun ofLocation(location: Location): BukkitCtx {
            return BukkitCtx({ Bukkit.getServer().isOwnedByCurrentRegion(location) }, { runnable ->
                Bukkit.getRegionScheduler().run(AquaticCommon.plugin, location) { runnable.run() }
            })
        }

        fun ofEntity(entity: Entity): BukkitCtx {
            return BukkitCtx({ Bukkit.getServer().isOwnedByCurrentRegion(entity) }, { runnable ->
                entity.scheduler.run(AquaticCommon.plugin, { runnable.run() }, null)
            })
        }
    }
}