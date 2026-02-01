package gg.aquatic.kevent.subscription

import gg.aquatic.kevent.EventPriority

interface SubscriptionInfo<T> {
    val name: String
    val eventType: Class<T>
    val priority: EventPriority
    val ignoreCancelled: Boolean

    override fun toString(): String
}
