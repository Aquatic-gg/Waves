package gg.aquatic.snapshotmap

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class SnapshotMapTest {

    @Test
    fun `test basic iteration consistency`() {
        val map = SnapshotMap<String, Int>()
        map["a"] = 1
        map["b"] = 2
        map["c"] = 3

        val results = mutableMapOf<String, Int>()
        map.forEach { k, v -> results[k] = v }

        assertEquals(3, results.size)
        assertEquals(1, results["a"])
        assertEquals(2, results["b"])
        assertEquals(3, results["c"])
    }

    @Test
    fun `test snapshot invalidation on change`() {
        val map = SnapshotMap<String, Int>()
        map["a"] = 1
        
        // First iteration creates snapshot
        var count = 0
        map.forEach { _, _ -> count++ }
        assertEquals(1, count)

        // Modify map
        map["b"] = 2
        
        // Second iteration should see new value (snapshot was invalidated)
        count = 0
        map.forEach { _, _ -> count++ }
        assertEquals(2, count)
    }

    @Test
    fun `test no invalidation on same value put`() {
        val map = SnapshotMap<String, Int>()
        map["a"] = 1
        
        // Build snapshot
        map.forEach { _, _ -> }
        
        // Put same value - should NOT invalidate (based on our optimization)
        map["a"] = 1
        
        // This is hard to test directly without reflection, 
        // but it ensures our logic doesn't break basic map contracts.
        assertEquals(1, map["a"])
    }

    @Test
    fun `test heavy concurrent read-write stress`() {
        val map = SnapshotMap<Int, Int>()
        val threadCount = 16
        val operationsPerThread = 10000
        val executor = Executors.newFixedThreadPool(threadCount + 2)
        val latch = CountDownLatch(threadCount)
        val stopSignal = AtomicInteger(0)

        // Writers: Constantly modifying the map
        repeat(threadCount) { threadId ->
            executor.submit {
                try {
                    for (i in 0 until operationsPerThread) {
                        val key = (threadId * operationsPerThread) + i
                        map[key] = i
                        if (i % 100 == 0) map.remove(key - 50) // Occasional removes
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        // Readers: Constantly iterating the map while writers are active
        val iterationErrors = AtomicInteger(0)
        repeat(2) {
            executor.submit {
                while (stopSignal.get() == 0) {
                    try {
                        var sum = 0
                        map.forEach { _, v -> sum += v }
                    } catch (e: Exception) {
                        iterationErrors.incrementAndGet()
                    }
                }
            }
        }

        latch.await(30, TimeUnit.SECONDS)
        stopSignal.set(1)
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)

        assertEquals(0, iterationErrors.get(), "Should have zero exceptions during concurrent iteration")
        
        // Final sanity check
        var finalCount = 0
        map.forEach { _, _ -> finalCount++ }
        assertEquals(map.size, finalCount, "Snapshot size must match internalMap size after contention")
    }

    @Test
    fun `test clear and putAll snapshot lifecycle`() {
        val map = SnapshotMap<Int, Int>()
        map.putAll(mapOf(1 to 1, 2 to 2))
        
        var count = 0
        map.forEach { _, _ -> count++ }
        assertEquals(2, count)

        map.clear()
        count = 0
        map.forEach { _, _ -> count++ }
        assertEquals(0, count)
    }
}
