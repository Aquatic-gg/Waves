package gg.aquatic.snapshotmap

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun `test parallel chaos and consistency`() {
        val map = SnapshotMap<Int, Int>()
        val threadCount = 32
        val itemsPerThread = 2000
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val errors = AtomicInteger(0)

        // Parallel writers and readers fighting for the snapshot
        for (t in 0 until threadCount) {
            executor.submit {
                try {
                    for (i in 0 until itemsPerThread) {
                        val key = (t * itemsPerThread) + i

                        // Concurrent writes
                        map[key] = key

                        // Frequent parallel iterations (forcing snapshot rebuilds)
                        if (i % 50 == 0) {
                            var iterationCount = 0
                            map.forEach { _, _ -> iterationCount++ }
                            // We don't assert size here because it's weakly consistent,
                            // but we ensure it doesn't crash or throw exceptions.
                        }

                        // Concurrent removes
                        if (i % 100 == 0) {
                            map.remove(key)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    errors.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(20, TimeUnit.SECONDS), "Test timed out under contention")
        executor.shutdown()

        assertEquals(0, errors.get(), "Parallel chaos caused exceptions in SnapshotMap")

        // Final Verification: Once activity stops, the snapshot MUST be correct
        val finalMapSize = map.size
        var finalIterationCount = 0
        map.forEach { _, _ -> finalIterationCount++ }

        assertEquals(finalMapSize, finalIterationCount, "Final snapshot size mismatch after chaos stopped")
    }

    @Test
    fun `test extreme atomic integrity chaos`() {
        val map = SnapshotMap<Int, Int>()
        val threadCount = 32
        val itemsPerThread = 100000
        val totalExpectedItems = threadCount * itemsPerThread
        val executor = Executors.newFixedThreadPool(threadCount + 4)
        val latch = CountDownLatch(threadCount)

        // Truth tracking: 0 = not in map, 1 = in map
        val truth = Array(totalExpectedItems) { AtomicInteger(0) }
        val exceptions = java.util.concurrent.CopyOnWriteArrayList<Throwable>()

        // 1. Extreme Writers: Toggling bits and map entries
        for (t in 0 until threadCount) {
            val startKey = t * itemsPerThread
            executor.submit {
                try {
                    for (i in 0 until itemsPerThread) {
                        val key = startKey + i

                        // Add to map and update truth
                        map[key] = key
                        truth[key].set(1)

                        // Stress the versioning by immediately updating some
                        if (i % 3 == 0) {
                            map[key] = key + 1
                        }

                        // Stress the invalidation with removes
                        if (i % 10 == 0) {
                            map.remove(key)
                            truth[key].set(0)
                        }
                    }
                } catch (e: Throwable) {
                    exceptions.add(e)
                } finally {
                    latch.countDown()
                }
            }
        }

        // 2. High-Frequency Chaos Readers
        val stopSignal = AtomicInteger(0)
        repeat(4) {
            executor.submit {
                while (stopSignal.get() == 0) {
                    try {
                        var localCount = 0
                        map.forEach { _, _ -> localCount++ }
                        // We don't check localCount here because CHM is weakly consistent,
                        // but we ensure it never crashes.
                    } catch (e: Throwable) {
                        exceptions.add(e)
                    }
                }
            }
        }

        assertTrue(latch.await(60, TimeUnit.SECONDS), "Integrity test timed out")
        stopSignal.set(1)
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)

        // --- FINAL INTEGRITY VERIFICATION ---

        assertTrue(exceptions.isEmpty(), "Exceptions caught during chaos: ${exceptions.firstOrNull()?.message}")

        val expectedSize = truth.count { it.get() == 1 }
        val actualMapSize = map.size

        // Calculate snapshot size
        var actualSnapshotSize = 0
        map.forEach { _, _ -> actualSnapshotSize++ }

        assertEquals(expectedSize, actualMapSize, "Final map size does not match atomic truth")
        assertEquals(actualMapSize, actualSnapshotSize, "Final snapshot size is inconsistent with internal map")

        // Deep verify every single key
        for (i in 0 until totalExpectedItems) {
            val shouldExist = truth[i].get() == 1
            val existsInMap = map.containsKey(i)
            assertEquals(shouldExist, existsInMap, "Integrity failure at key $i: expected presence $shouldExist")
        }
    }
}
