package gg.aquatic.kevent.subscription

import gg.aquatic.kevent.SuspendingEventListener

interface SuspendingSubscription<T> : SubscriptionInfo<T> {

    /**
     * Returns the listener if it's still available, or null if it was collected (for weak subs).
     */
    fun getListener(): SuspendingEventListener<T>?
}
