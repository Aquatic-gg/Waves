package gg.aquatic.kevent

import gg.aquatic.kevent.subscription.Subscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

interface EventBus {

    fun <T : Any> post(event: T): Deferred<PostResult<T>>

    fun <T> subscribe(
        eventType: Class<T>,
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        listener: EventListener<T>,
    ): Subscription<T>

    fun <T> subscribeWeak(
        eventType: Class<T>,
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        listener: EventListener<T>,
    ): Subscription<T>

    fun <T> unregister(subscription: Subscription<T>)
    fun getScope(): CoroutineScope

    fun getEventExceptionHandler(): EventExceptionHandler

    operator fun invoke(eventRegisterBuilder: EventRegisterBuilder.() -> Unit) =
        EventRegisterBuilder(this).apply(eventRegisterBuilder)

    class EventRegisterBuilder internal constructor(val eventBus: EventBus) {
        fun <T> strong(
            eventType: Class<T>,
            priority: EventPriority = EventPriority.NORMAL,
            ignoreCancelled: Boolean = false,
            listener: EventListener<T>,
        ): Subscription<T> {
            return eventBus.subscribe(eventType, priority, ignoreCancelled, listener)
        }

        fun <T> weak(
            eventType: Class<T>,
            priority: EventPriority = EventPriority.NORMAL,
            ignoreCancelled: Boolean = false,
            listener: EventListener<T>,
        ): Subscription<T> {
            return eventBus.subscribeWeak(eventType, priority, ignoreCancelled, listener)
        }
    }
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
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    listener: EventListener<T>,
): Subscription<T> = subscribe(T::class.java, priority, ignoreCancelled, listener)

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
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    listener: EventListener<T>,
): Subscription<T> = subscribeWeak(T::class.java, priority, ignoreCancelled, listener)


/**
 * Subscribes to an event type, registering the provided listener to handle events of that type.
 *
 * @param T The type of event to subscribe to.
 * @param listener The event listener that will be invoked when an event of the specified type is published.
 * @param priority The priority level of the listener, defining its execution order relative to other listeners. Defaults to `EventPriority.NORMAL`.
 * @return A `Subscription` representing the registration, which can be used to manage or unregister the listener.
 */
inline fun <reified T> EventBus.EventRegisterBuilder.strong(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    listener: EventListener<T>,
): Subscription<T> = strong(T::class.java, priority, ignoreCancelled, listener)

/**
 * Subscribes a listener to a specific event type with a weak reference,
 * allowing the listener to be garbage collected if no strong references exist to it.
 *
 * @param T The type of event the listener will handle.
 * @param listener The event listener to be invoked when events of the specified type are published.
 * @param priority The priority of the subscription, determining the order of execution relative to other listeners. Defaults to `EventPriority.NORMAL`.
 * @return A `Subscription` object representing the subscription, which can be used to unregister or query the listener's status.
 */
inline fun <reified T> EventBus.EventRegisterBuilder.weak(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    listener: EventListener<T>,
): Subscription<T> = weak(T::class.java, priority, ignoreCancelled, listener)