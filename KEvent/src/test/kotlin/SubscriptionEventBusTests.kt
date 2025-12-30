package gg.aquatic.kevent

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

open class TestEvent
class SubTestEvent : TestEvent()
class CancellableEvent(override var cancelled: Boolean = false) : Cancellable

class SubscriptionTests {

    private fun createBus(): EventBus = eventBusBuilder {
        hierarchical = true
    }

    @Test
    fun `test strong subscription`(): Unit = runBlocking {
        val bus = createBus()
        var called = false
        bus.subscribe<TestEvent> { called = true }
        
        bus.postSuspend(TestEvent())
        assertTrue(called, "Strong subscription should be called")
    }

    @Test
    fun `test weak subscription garbage collection`(): Unit = runBlocking {
        val bus = createBus()
        var callCount = 0

        var listener: EventListener<TestEvent>? = EventListener { callCount++ }

        bus.subscribeWeak<TestEvent>(listener = listener!!)
        bus.postSuspend(TestEvent())
        assertEquals(1, callCount, "Should be called while listener is alive")

        listener = null

        repeat(10) {
            System.gc()
            System.runFinalization()
            // Allocation of some "garbage" to trigger GC
            ByteArray(1024 * 1024)
        }

        bus.postSuspend(TestEvent())
        assertEquals(1, callCount, "Weak subscription should not be called after GC")
    }

    @Test
    fun `test priority order`() = runBlocking {
        val bus = createBus()
        val results = mutableListOf<String>()

        bus.subscribe<TestEvent>(priority = EventPriority.LOW) { results.add("LOW") }
        bus.subscribe<TestEvent>(priority = EventPriority.HIGHEST) { results.add("HIGHEST") }
        bus.subscribe<TestEvent>(priority = EventPriority.MONITOR) { results.add("MONITOR") }
        bus.subscribe<TestEvent>(priority = EventPriority.NORMAL) { results.add("NORMAL") }

        bus.postSuspend(TestEvent())

        assertEquals(listOf("HIGHEST", "NORMAL", "LOW", "MONITOR"), results)
    }

    @Test
    fun `test cancellation logic`() = runBlocking {
        val bus = createBus()
        var normalCalled = false
        var ignoreCancelledCalled = false

        bus.subscribe<CancellableEvent>(priority = EventPriority.HIGHEST) { 
            it.cancelled = true 
        }

        // This listener should be skipped because the event is canceled
        bus.subscribe<CancellableEvent>(priority = EventPriority.NORMAL, ignoreCancelled = false) {
            normalCalled = true
        }

        // This listener should be called anyway
        bus.subscribe<CancellableEvent>(priority = EventPriority.LOW, ignoreCancelled = true) {
            ignoreCancelledCalled = true
        }

        bus.postSuspend(CancellableEvent())

        assertTrue(!normalCalled, "Normal listener should NOT be called on cancelled event")
        assertTrue(ignoreCancelledCalled, "Listener with ignoreCancelled=true SHOULD be called")
    }

    @Test
    fun `test hierarchical dispatch`() = runBlocking {
        val bus = createBus()
        var callCount = 0

        bus.subscribe<TestEvent> {
            callCount++
        }
        bus.postSuspend(SubTestEvent())

        assertEquals(1, callCount, "Parent listener should receive child event when hierarchical is true")
    }
}
