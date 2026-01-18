package gg.aquatic.snapshotmap

import java.util.concurrent.ConcurrentHashMap

sealed class AbstractSnapshotMap<K : Any, V : Any>(
    @PublishedApi internal val internalMap: ConcurrentHashMap<K, V> = ConcurrentHashMap()
) : MutableMap<K, V> by internalMap {

    @PublishedApi
    internal class Snapshot(val keys: Array<Any?>, val values: Array<Any?>, val version: Long)

    @Volatile
    @PublishedApi
    internal var snapshot: Snapshot? = null

    @Volatile
    @PublishedApi
    internal var version: Long = 0

    protected fun invalidate() {
        version++
        snapshot = null
    }

    override fun put(key: K, value: V): V? {
        val prev = internalMap.put(key, value)
        if (prev != value) invalidate()
        return prev
    }

    override fun remove(key: K): V? {
        val prev = internalMap.remove(key)
        if (prev != null) invalidate()
        return prev
    }

    override fun putAll(from: Map<out K, V>) {
        internalMap.putAll(from)
        invalidate()
    }

    override fun clear() {
        internalMap.clear()
        invalidate()
    }

    override fun remove(key: K, value: V): Boolean {
        return internalMap.remove(key, value).also { if (it) invalidate() }
    }

    override val keys: MutableSet<K> get() = SnapshotKeySet()
    override val values: MutableCollection<V> get() = SnapshotValueCollection()
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = SnapshotEntrySet()

    internal abstract fun getOrComputeSnapshot(): Snapshot

    @Suppress("UNCHECKED_CAST")
    internal fun createSnapshotUnderLock(): Snapshot {
        val startVersion = version
        val size = internalMap.size
        val ks = arrayOfNulls<Any>(size)
        val vs = arrayOfNulls<Any>(size)

        var i = 0
        for ((k, v) in internalMap) {
            if (i >= size) break
            ks[i] = k
            vs[i] = v
            i++
        }

        val finalKs = if (i < size) ks.copyOf(i) else ks
        val finalVs = if (i < size) vs.copyOf(i) else vs

        val newSnapshot = Snapshot(finalKs as Array<Any?>, finalVs as Array<Any?>, startVersion)
        if (startVersion == version) {
            snapshot = newSnapshot
        }
        return newSnapshot
    }

    private inner class SnapshotKeySet : MutableSet<K> by internalMap.keys {
        override fun iterator(): MutableIterator<K> {
            val snap = getOrComputeSnapshot()
            return object : MutableIterator<K> {
                private var index = 0
                override fun hasNext() = index < snap.keys.size
                override fun next() = snap.keys[index++] as K
                override fun remove() = this@AbstractSnapshotMap.remove(snap.keys[index - 1] as K).let { }
            }
        }
    }

    private inner class SnapshotValueCollection : MutableCollection<V> by internalMap.values {
        override fun iterator(): MutableIterator<V> {
            val snap = getOrComputeSnapshot()
            return object : MutableIterator<V> {
                private var index = 0
                override fun hasNext() = index < snap.values.size
                override fun next() = snap.values[index++] as V
                override fun remove() = throw UnsupportedOperationException()
            }
        }
    }

    private inner class SnapshotEntrySet : MutableSet<MutableMap.MutableEntry<K, V>> by internalMap.entries {
        override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> {
            val snap = getOrComputeSnapshot()
            return object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                private var index = 0
                override fun hasNext() = index < snap.keys.size
                override fun next() = SnapshotEntry(snap.keys[index] as K, snap.values[index++] as V)
                override fun remove() = this@AbstractSnapshotMap.remove(snap.keys[index - 1] as K).let { }
            }
        }
    }

    private inner class SnapshotEntry(override val key: K, override var value: V) : MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V {
            val old = value
            this@AbstractSnapshotMap[key] = newValue
            value = newValue
            return old
        }
    }
}