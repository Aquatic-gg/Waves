package gg.aquatic.kmenu.bukkit

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventException
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.Plugin

internal inline fun <reified T : Event> registerBukkitEvent(
    plugin: Plugin,
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    crossinline handler: (T) -> Unit
) {
    val listener = object : Listener {}
    val executor = EventExecutor { _, event ->
        if (event is T) {
            try {
                handler(event)
            } catch (ex: Exception) {
                throw EventException(ex)
            }
        }
    }

    Bukkit.getPluginManager().registerEvent(
        T::class.java,
        listener,
        priority,
        executor,
        plugin,
        ignoreCancelled
    )
}

internal fun runOnMainThread(plugin: Plugin, block: () -> Unit) {
    if (Bukkit.isPrimaryThread()) {
        block()
        return
    }
    Bukkit.getScheduler().runTask(plugin, Runnable(block))
}
