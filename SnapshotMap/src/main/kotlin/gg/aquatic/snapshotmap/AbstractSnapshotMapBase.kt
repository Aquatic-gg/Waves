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
        if (prev != value) {
            invalidate()
        }
        return prev
    }

    override fun remove(key: K): V? {
        val prev = internalMap.remove(key)
        if (prev != null) {
            invalidate()
        }
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
}
