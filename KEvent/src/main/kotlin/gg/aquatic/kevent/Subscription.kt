package gg.aquatic.kevent

import gg.aquatic.kevent.subscription.Subscription

class Subscription<T>(
    val listener: EventListener<T>,
    val name: String,
    val eventType: Class<T>,
    val priority: EventPriority
) {

    @Suppress("UNCHECKED_CAST")
    fun <A> shouldInvoke(hierarchical: Boolean, eventType: Class<A>): Subscription<A>? {
        return if (hierarchical) {
            if (this.eventType.isAssignableFrom(eventType)) this as Subscription<A>
            else null
        } else if (this.eventType == eventType) this as Subscription<A>
        else null
    }

    override fun toString(): String {
        return "Subscription($name, priority=$priority)"
    }
}