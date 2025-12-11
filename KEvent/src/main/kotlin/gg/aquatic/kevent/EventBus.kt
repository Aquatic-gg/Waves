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

/**
 * Subscribes to an event type, registering the provided listener to handle events of that type.
 *
 * @param T The type of event to subscribe to.
 * @param listener The event listener that will be invoked when an event of the specified type is published.
 * @param priority The priority level of the listener, defining its execution order relative to other listeners. Defaults to `EventPriority.NORMAL`.
 * @return A `Subscription` representing the registration, which can be used to manage or unregister the listener.
 */
inline fun <reified T> EventBus.subscribe(
    listener: EventListener<T>,
    priority: EventPriority = EventPriority.NORMAL
): Subscription<T> = subscribe(T::class.java, listener, priority)

/**
 * Subscribes a listener to a specific event type with a weak reference,
 * allowing the listener to be garbage collected if no strong references exist to it.
 *
 * @param T The type of event the listener will handle.
 * @param listener The event listener to be invoked when events of the specified type are published.
 * @param priority The priority of the subscription, determining the order of execution relative to other listeners. Defaults to `EventPriority.NORMAL`.
 * @return A `Subscription` object representing the subscription, which can be used to unregister or query the listener's status.
 */
inline fun <reified T> EventBus.subscribeWeak(
    listener: EventListener<T>,
    priority: EventPriority = EventPriority.NORMAL
): Subscription<T> = subscribeWeak(T::class.java, listener, priority)