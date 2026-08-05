package gg.aquatic.kevent.subscription

import gg.aquatic.kevent.EventListener

interface Subscription<T> : SubscriptionInfo<T> {

    /**
     * Returns the listener if it's still available, or null if it was collected (for weak subs).
     */
    fun getListener(): EventListener<T>?
}
