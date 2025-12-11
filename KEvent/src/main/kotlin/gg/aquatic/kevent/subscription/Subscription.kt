package gg.aquatic.kevent.subscription

import gg.aquatic.kevent.EventListener
import gg.aquatic.kevent.EventPriority

interface Subscription<T> {
    val name: String
    val eventType: Class<T>
    val priority: EventPriority

    /**
     * Returns the listener if it's still available, or null if it was collected (for weak subs).
     */
    fun getListener(): EventListener<T>?

    override fun toString(): String
}
