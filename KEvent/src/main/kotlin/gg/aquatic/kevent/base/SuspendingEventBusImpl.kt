package gg.aquatic.kevent.base

import gg.aquatic.kevent.*
import gg.aquatic.kevent.subscription.SuspendingStrongSubscription
import gg.aquatic.kevent.subscription.SuspendingSubscription
import gg.aquatic.kevent.subscription.SuspendingWeakSubscription
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class SuspendingEventBusImpl(
    private val exceptionHandler: EventExceptionHandler,
    private val scope: CoroutineScope,
    private val hierarchical: Boolean
) : SuspendingEventBus {

    // Cache lock - thread safety
    private val lock = Any()
    private val subscriptions = ConcurrentHashMap<Class<*>, MutableList<SuspendingSubscription<*>>>()
    private val allSubscriptions = CopyOnWriteArrayList<SuspendingSubscription<*>>()

    private data class CacheKey(val eventClass: Class<*>, val hierarchical: Boolean)

    private val dispatchCache = ConcurrentHashMap<CacheKey, List<SuspendingSubscription<*>>>()

    override suspend fun <T : Any> postSuspend(event: T): PostResult<T, SuspendingSubscription<T>> {
        val context = scope.coroutineContext.minusKey(Job)
        return withContext(context) { dispatch(event) }
    }

    override fun <T : Any> post(event: T): CompletableFuture<PostResult<T, SuspendingSubscription<T>>> {
        return scope.async { dispatch(event) }.asCompletableFuture()
    }

    /**
     * Posts event; measures handler execution; returns a measured result
     */
    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> dispatch(event: T): PostResult<T, SuspendingSubscription<T>> {
        val executionTimes = HashMap<SuspendingSubscription<T>, Long>()
        var failed = 0
        var success = 0

        val eventClass = event::class.java
        val key = CacheKey(eventClass, hierarchical)
        val toHandle = dispatchCache[key] ?: buildDispatchList(eventClass, hierarchical).also {
            dispatchCache[key] = it
        }

        for (rawSub in toHandle) {
            val sub = rawSub as SuspendingSubscription<T>
            val listener = sub.getListener()
            if (listener == null) {
                // Weak subscription whose target was GC'd -> unregister it
                unregister(sub)
                continue
            }

            if (!sub.ignoreCancelled && event is Cancellable) {
                if (event.cancelled) {
                    continue
                }
            }

            try {
                val time = System.nanoTime()
                listener.handle(event)
                executionTimes[sub] = System.nanoTime() - time
                success++
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                failed++
                exceptionHandler.handleException(sub, event, ex)
            }
        }

        return BasicMeasuredPostResult(event, success, failed, executionTimes)
    }

    private fun buildDispatchList(
        eventClass: Class<*>,
        hierarchical: Boolean
    ): List<SuspendingSubscription<*>> {
        val result = ArrayList<SuspendingSubscription<*>>()
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
        priority: EventPriority,
        ignoreCancelled: Boolean,
        listener: SuspendingEventListener<T>,
    ): SuspendingSubscription<T> {
        val subscription = SuspendingStrongSubscription(listener, listener::class.java.name, eventType, priority, ignoreCancelled)
        addSubscription(subscription)
        return subscription
    }

    override fun <T> subscribeWeak(
        eventType: Class<T>,
        priority: EventPriority,
        ignoreCancelled: Boolean,
        listener: SuspendingEventListener<T>,
    ): SuspendingSubscription<T> {
        val subscription = SuspendingWeakSubscription(listener, listener::class.java.name, eventType, priority, ignoreCancelled)
        addSubscription(subscription)
        return subscription
    }

    override fun <T> unregister(subscription: SuspendingSubscription<T>) {
        synchronized(lock) {
            allSubscriptions.remove(subscription)
            val subs = subscriptions[subscription.eventType]
            if (subs != null) {
                subs -= subscription
                if (subs.isEmpty()) subscriptions.remove(subscription.eventType)
            }
            dispatchCache.clear()
        }
    }

    private fun addSubscription(subscription: SuspendingSubscription<*>) {
        synchronized(lock) {
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
    }

    override fun getEventExceptionHandler(): EventExceptionHandler = exceptionHandler
    override fun getScope(): CoroutineScope = scope
}
