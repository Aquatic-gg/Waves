package gg.aquatic.statistik

import gg.aquatic.common.event
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

abstract class ListenerStatisticType<T : Any> : StatisticType<T>() {
    private var listener: Listener? = null

    /**
     * Define the event logic here using the 'event' helper
     */
    abstract fun createListener(): Listener

    override fun initialize() {
        listener = createListener()
    }

    override fun terminate() {
        listener?.let { HandlerList.unregisterAll(it) }
        listener = null
    }

    // Helper to keep the syntax clean in implementations
    protected inline fun <reified E : Event> listen(
        ignoreCancelled: Boolean = true,
        crossinline block: (E) -> Unit
    ): Listener = event<E>(ignoredCancelled = ignoreCancelled, callback = block)
}