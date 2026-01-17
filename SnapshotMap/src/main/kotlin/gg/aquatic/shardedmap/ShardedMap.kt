package gg.aquatic.shardedmap

import jdk.internal.vm.annotation.Contended
import java.util.concurrent.atomic.LongAdder

class ShardedMap<K, V>(
    shardCount: Int = defaultShardCount()
) {

    private val shardSize: Int = nextPowerOfTwo(shardCount)
    private val mask: Int = shardSize - 1
    private val shards: Array<Segment<K, V>> = Array(shardSize) { Segment() }
    private val size = LongAdder()

    operator fun get(key: K): V? = shardFor(key).get(key)
    operator fun set(key: K, value: V): V? {
        val segment = shardFor(key)
        val previous = segment.put(key, value)
        if (previous == null) size.increment()
        return previous
    }

    fun put(key: K, value: V): V? = set(key, value)

    operator fun minusAssign(key: K) {
        remove(key)
    }

    fun remove(key: K): V? {
        val segment = shardFor(key)
        val removed = segment.remove(key)
        if (removed != null) size.decrement()
        return removed
    }

    fun containsKey(key: K): Boolean = shardFor(key).containsKey(key)

    fun size(): Long = size.sum()

    fun clear() {
        for (segment in shards) {
            val removed = segment.clear()
            if (removed > 0) size.add((-removed).toLong())
        }
    }

    /**
     * Weakly-consistent iteration without snapshots nor allocation
     */
    fun forEach(action: (K, V) -> Unit) {
        for (segment in shards) segment.forEach(action)
    }

    fun forEachKey(action: (K) -> Unit) {
        for (segment in shards) segment.forEachKey(action)
    }

    fun forEachValue(action: (V) -> Unit) {
        for (segment in shards) segment.forEachValue(action)
    }

    fun snapshot(): Map<K, V> {
        val snapshot = HashMap<K, V>()
        for (segment in shards) segment.forEach { key, value -> snapshot[key] = value }
        return snapshot
    }

    private fun shardFor(key: K): Segment<K, V> {
        val hash = smear(key.hashCode())
        return shards[hash and mask]
    }

    @Suppress("unused")
    private class Segment<K, V> {

        private var p0: Long = 0; private var p1: Long = 0; private var p2: Long = 0; private var p3: Long = 0
        private var p4: Long = 0; private var p5: Long = 0; private var p6: Long = 0; private var p7: Long = 0

        private val lock = Any()
        private val map = HashMap<K, V>()

        private var p8: Long = 0; private var p9: Long = 0; private var p10: Long = 0; private var p11: Long = 0
        private var p12: Long = 0; private var p13: Long = 0; private var p14: Long = 0; private var p15: Long = 0

        fun get(key: K): V? = synchronized(lock) { map[key] }
        fun put(key: K, value: V) = synchronized(lock) { map.put(key, value) }
        fun remove(key: K) = synchronized(lock) { map.remove(key) }
        fun containsKey(key: K) = synchronized(lock) { map.containsKey(key) }
        fun clear(): Int {
            val size = map.size
            map.clear()
            return size
        }

        fun forEach(action: (K, V) -> Unit) = synchronized(lock) { map.forEach(action) }
        fun forEachKey(action: (K) -> Unit) = synchronized(lock) { map.keys.forEach(action) }
        fun forEachValue(action: (V) -> Unit) = synchronized(lock) { map.values.forEach(action) }
    }

    companion object {
        private fun defaultShardCount() = nextPowerOfTwo(Runtime.getRuntime().availableProcessors() * 2)

        private fun nextPowerOfTwo(value: Int): Int {
            var v = value - 1
            v = v or (v shr 1)
            v = v or (v shr 2)
            v = v or (v shr 4)
            v = v or (v shr 8)
            v = v or (v shr 16)
            return (v + 1).coerceAtLeast(2)
        }

        private fun smear(hash: Int): Int = hash xor (hash ushr 16)
    }
}