package gg.aquatic.kevent.subscription

import gg.aquatic.kevent.EventListener
import gg.aquatic.kevent.EventPriority

class StrongSubscription<T>(
    private val listener: EventListener<T>,
    override val name: String,
    override val eventType: Class<T>,
    override val priority: EventPriority,
    override val ignoreCancelled: Boolean = false
) : Subscription<T> {

    override fun getListener(): EventListener<T> = listener

    override fun toString(): String {
        return "StrongSubscription($name, priority=$priority, ignoreCancelled=$ignoreCancelled)"
    }
}
