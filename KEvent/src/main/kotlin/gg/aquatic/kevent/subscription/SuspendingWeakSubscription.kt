package gg.aquatic.kevent.subscription

import gg.aquatic.kevent.EventPriority
import gg.aquatic.kevent.SuspendingEventListener
import java.lang.ref.WeakReference

class SuspendingWeakSubscription<T>(
    listener: SuspendingEventListener<T>,
    override val name: String,
    override val eventType: Class<T>,
    override val priority: EventPriority,
    override val ignoreCancelled: Boolean
) : SuspendingSubscription<T> {

    private val ref = WeakReference(listener)

    override fun getListener(): SuspendingEventListener<T>? = ref.get()

    override fun toString(): String {
        return "SuspendingWeakSubscription($name, priority=$priority, ignoreCancelled=$ignoreCancelled)"
    }
}
