package gg.aquatic.kevent.subscription

import gg.aquatic.kevent.EventPriority
import gg.aquatic.kevent.SuspendingEventListener

class SuspendingStrongSubscription<T>(
    private val listener: SuspendingEventListener<T>,
    override val name: String,
    override val eventType: Class<T>,
    override val priority: EventPriority,
    override val ignoreCancelled: Boolean
) : SuspendingSubscription<T> {

    override fun getListener(): SuspendingEventListener<T> = listener

    override fun toString(): String {
        return "SuspendingStrongSubscription($name, priority=$priority, ignoreCancelled=$ignoreCancelled)"
    }
}
