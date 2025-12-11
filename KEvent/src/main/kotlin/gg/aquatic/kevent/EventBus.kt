package gg.aquatic.kevent

import gg.aquatic.kevent.subscription.Subscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

interface EventBus {

    fun <T : Any> post(event: T): Deferred<PostResult<T>>

    fun <T> subscribe(
        eventType: Class<T>,
        listener: EventListener<T>,
        priority: EventPriority = EventPriority.NORMAL
    ): Subscription<T>

    fun <T> subscribeWeak(
        eventType: Class<T>,
        listener: EventListener<T>,
        priority: EventPriority = EventPriority.NORMAL
    ): Subscription<T>

    fun <T> unregister(subscription: Subscription<T>)
    fun getScope(): CoroutineScope

    fun getEventExceptionHandler(): EventExceptionHandler
}

inline fun <reified T> EventBus.subscribe(
    listener: EventListener<T>,
    priority: EventPriority = EventPriority.NORMAL
): Subscription<T> = subscribe(T::class.java, listener, priority)

inline fun <reified T> EventBus.subscribeWeak(
    listener: EventListener<T>,
    priority: EventPriority = EventPriority.NORMAL
): Subscription<T> = subscribeWeak(T::class.java, listener, priority)