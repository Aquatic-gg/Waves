package gg.aquatic.shardedmap

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*

class ShardedMapTest {

    @Test
    fun `test single-threaded basic operations`() {
        val map = ShardedMap<String, Int>(shardCount = 4)
        map["apple"] = 1
        map["banana"] = 2

        assertEquals(1, map["apple"])
        assertEquals(2, map["banana"])
        assertEquals(2L, map.size())
        assertTrue(map.containsKey("apple"))

        map.remove("apple")
        assertNull(map["apple"])
        assertEquals(1L, map.size())
    }

    @Test
    fun `test heavy concurrent writes`() {
        val map = ShardedMap<Int, String>(shardCount = 16)
        val threadCount = 32
        val itemsPerThread = 5000
        val totalItems = threadCount * itemsPerThread
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        for (t in 0 until threadCount) {
            executor.submit {
                try {
                    for (i in 0 until itemsPerThread) {
                        // Each thread writes to unique keys
                        val key = t * itemsPerThread + i
                        map[key] = "value-$key"
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()

        assertEquals(totalItems.toLong(), map.size(), "Map size should match total items added")
        
        // Verify a sample of items
        for (i in 0 until totalItems step 100) {
            assertNotNull(map[i], "Key $i should exist")
        }
    }

    @Test
    fun `test concurrent read write and remove`() {
        val map = ShardedMap<Int, Int>(shardCount = 32)
        val runtimeMillis = 2000L
        val threadCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(4)
        val executor = Executors.newFixedThreadPool(threadCount)
        val stopSignal = AtomicInteger(0)

        // Writers
        repeat(threadCount / 2) {
            executor.submit {
                while (stopSignal.get() == 0) {
                    val key = (0..1000).random()
                    map[key] = key
                }
            }
        }

        // Removers
        repeat(threadCount / 4) {
            executor.submit {
                while (stopSignal.get() == 0) {
                    val key = (0..1000).random()
                    map.remove(key)
                }
            }
        }

        // Readers
        repeat(threadCount / 4) {
            executor.submit {
                while (stopSignal.get() == 0) {
                    val key = (0..1000).random()
                    map[key]
                }
            }
        }

        Thread.sleep(runtimeMillis)
        stopSignal.set(1)
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)

        // After all that chaos, size should still be consistent with the actual content
        var count = 0L
        map.forEach { _, _ -> count++ }
        assertEquals(count, map.size(), "Internal size counter must match actual item count after contention")
    }
}
