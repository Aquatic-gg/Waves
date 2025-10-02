package gg.aquatic.waves.util.task

import gg.aquatic.waves.Waves
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

object BukkitCtx : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        Bukkit.getScheduler().runTask(Waves.INSTANCE, block)
    }
}