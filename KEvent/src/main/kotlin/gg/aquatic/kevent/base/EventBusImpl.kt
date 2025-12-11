package gg.aquatic.kevent.base

import gg.aquatic.kevent.*
import gg.aquatic.kevent.subscription.StrongSubscription
import gg.aquatic.kevent.subscription.Subscription
import gg.aquatic.kevent.subscription.WeakSubscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class EventBusImpl(
    private val exceptionHandler: EventExceptionHandler,
    private val scope: CoroutineScope,
    private val hierarchical: Boolean
): EventBus {

    private val subscriptions = ConcurrentHashMap<Class<*>, MutableList<Subscription<*>>>()
    private val allSubscriptions = CopyOnWriteArrayList<Subscription<*>>()

    private data class CacheKey(val eventClass: Class<*>, val hierarchical: Boolean)
    private val dispatchCache = ConcurrentHashMap<CacheKey, List<Subscription<*>>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T: Any> post(event: T): Deferred<PostResult<T>> {
        return scope.async {
            val executionTimes = HashMap<Subscription<T>, Long>()
            var failed = 0
            var success = 0

            val eventClass = event::class.java
            val key = CacheKey(eventClass, hierarchical)
            val toHandle = dispatchCache[key] ?: buildDispatchList(eventClass, hierarchical).also {
                dispatchCache[key] = it
            }

            for (rawSub in toHandle) {
                val sub = rawSub as Subscription<T>
                val listener = sub.getListener()
                if (listener == null) {
                    // Weak subscription whose target was GC'd -> unregister it
                    unregister(sub)
                    continue
                }
                try {
                    val time = System.currentTimeMillis()
                    listener.handle(event)
                    executionTimes[rawSub] = System.currentTimeMillis() - time
                    success++
                } catch (ex: Exception) {
                    failed++
                    exceptionHandler.handleException(rawSub, event, ex)
                }
            }

            BasicMeasuredPostResult(event, success, failed, executionTimes)
        }
    }

    private fun buildDispatchList(
        eventClass: Class<*>,
        hierarchical: Boolean
    ): List<Subscription<*>> {
        val result = ArrayList<Subscription<*>>()
        for (sub in allSubscriptions) {
            val targetType = sub.eventType
            val matches = if (hierarchical) {
                targetType.isAssignableFrom(eventClass)
            } else {
                targetType == eventClass
            }
            if (matches) {
                result += sub
            }
        }
        return result
    }

    override fun <T> subscribe(
        eventType: Class<T>,
        listener: EventListener<T>,
        priority: EventPriority
    ): Subscription<T> {
        val subscription = StrongSubscription(listener, listener::class.java.name, eventType, priority)
        addSubscription(subscription)
        return subscription
    }

    override fun <T> subscribeWeak(
        eventType: Class<T>,
        listener: EventListener<T>,
        priority: EventPriority
    ): Subscription<T> {
        val subscription = WeakSubscription(listener, listener::class.java.name, eventType, priority)
        addSubscription(subscription)
        return subscription
    }

    override fun <T> unregister(subscription: Subscription<T>) {
        allSubscriptions.remove(subscription)
        subscriptions[subscription.eventType]?.remove(subscription)
        dispatchCache.clear()
    }

    private fun addSubscription(subscription: Subscription<*>) {
        if (subscription.priority == EventPriority.MONITOR) {
            subscriptions.getOrPut(subscription.eventType) { CopyOnWriteArrayList() } += subscription
            allSubscriptions += subscription
            dispatchCache.clear()
            return
        }

        var idx = 0
        for (s in allSubscriptions) {
            if (s.priority === EventPriority.MONITOR) break
            if (s.priority.ordinal < subscription.priority.ordinal) break
            idx++
        }
        allSubscriptions.add(idx, subscription)

        val subsForType = subscriptions.getOrPut(subscription.eventType) { CopyOnWriteArrayList() }
        idx = 0
        for (s in subsForType) {
            if (s.priority === EventPriority.MONITOR) break
            if (s.priority.ordinal < subscription.priority.ordinal) break
            idx++
        }
        subsForType.add(idx, subscription)
        dispatchCache.clear()
    }

    override fun getEventExceptionHandler(): EventExceptionHandler = exceptionHandler
    override fun getScope(): CoroutineScope = scope
}