package gg.aquatic.kevent

import gg.aquatic.kevent.subscription.Subscription
import gg.aquatic.kevent.subscription.SuspendingSubscription
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.CompletableFuture

interface EventBus {

    fun <T : Any> post(event: T): PostResult<T, Subscription<T>>

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
 * Event bus variant that supports suspending posts and requires a non-null CoroutineScope.
 */
/**
 * Event bus variant that supports suspending posts and suspending listeners.
 */
interface SuspendingEventBus {

    suspend fun <T : Any> postSuspend(event: T): PostResult<T, SuspendingSubscription<T>>

    fun <T : Any> post(event: T): CompletableFuture<PostResult<T, SuspendingSubscription<T>>>

    fun <T> subscribe(
        eventType: Class<T>,
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        listener: SuspendingEventListener<T>,
    ): SuspendingSubscription<T>

    fun <T> subscribeWeak(
        eventType: Class<T>,
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        listener: SuspendingEventListener<T>,
    ): SuspendingSubscription<T>

    fun <T> unregister(subscription: SuspendingSubscription<T>)
    fun getScope(): CoroutineScope

    fun getEventExceptionHandler(): EventExceptionHandler

    operator fun invoke(eventRegisterBuilder: EventRegisterBuilder.() -> Unit) =
        EventRegisterBuilder(this).apply(eventRegisterBuilder)

    class EventRegisterBuilder internal constructor(private val eventBus: SuspendingEventBus) {
        fun <T> strong(
            eventType: Class<T>,
            priority: EventPriority = EventPriority.NORMAL,
            ignoreCancelled: Boolean = false,
            listener: SuspendingEventListener<T>,
        ): SuspendingSubscription<T> {
            return eventBus.subscribe(eventType, priority, ignoreCancelled, listener)
        }

        fun <T> weak(
            eventType: Class<T>,
            priority: EventPriority = EventPriority.NORMAL,
            ignoreCancelled: Boolean = false,
            listener: SuspendingEventListener<T>,
        ): SuspendingSubscription<T> {
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
@Suppress("unused")
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
@Suppress("unused")
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
@Suppress("unused")
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
@Suppress("unused")
inline fun <reified T> EventBus.EventRegisterBuilder.weak(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    listener: EventListener<T>,
): Subscription<T> = weak(T::class.java, priority, ignoreCancelled, listener)

/**
 * Subscribes to an event type with a suspending listener.
 */
@Suppress("unused")
inline fun <reified T> SuspendingEventBus.subscribe(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    listener: SuspendingEventListener<T>,
): SuspendingSubscription<T> = subscribe(T::class.java, priority, ignoreCancelled, listener)

/**
 * Subscribes to an event type with a suspending listener using a weak reference.
 */
@Suppress("unused")
inline fun <reified T> SuspendingEventBus.subscribeWeak(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    listener: SuspendingEventListener<T>,
): SuspendingSubscription<T> = subscribeWeak(T::class.java, priority, ignoreCancelled, listener)

/**
 * Subscribes to an event type with a suspending listener.
 */
@Suppress("unused")
inline fun <reified T> SuspendingEventBus.EventRegisterBuilder.strong(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    listener: SuspendingEventListener<T>,
): SuspendingSubscription<T> = strong(T::class.java, priority, ignoreCancelled, listener)

/**
 * Subscribes to an event type with a suspending listener using a weak reference.
 */
@Suppress("unused")
inline fun <reified T> SuspendingEventBus.EventRegisterBuilder.weak(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    listener: SuspendingEventListener<T>,
): SuspendingSubscription<T> = weak(T::class.java, priority, ignoreCancelled, listener)
