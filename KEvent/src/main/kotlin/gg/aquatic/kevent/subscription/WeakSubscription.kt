package gg.aquatic.kevent.subscription

import gg.aquatic.kevent.EventListener
import gg.aquatic.kevent.EventPriority
import java.lang.ref.WeakReference

class WeakSubscription<T>(
    listener: EventListener<T>,
    override val name: String,
    override val eventType: Class<T>,
    override val priority: EventPriority
) : Subscription<T> {

    private val ref = WeakReference(listener)

    override fun getListener(): EventListener<T>? = ref.get()

    override fun toString(): String {
        return "WeakSubscription($name, priority=$priority)"
    }
}
