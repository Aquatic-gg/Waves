package gg.aquatic.snapshotmap

import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiConsumer

class SnapshotMap<K: Any, V: Any>(
    private val internalMap: ConcurrentHashMap<K, V> = ConcurrentHashMap()
) : MutableMap<K, V> by internalMap {

    private class Snapshot(val keys: Array<Any?>, val values: Array<Any?>, val version: Long)

    @Volatile
    private var snapshot: Snapshot? = null

    @Volatile
    private var version: Long = 0

    private fun invalidate() {
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

    @Suppress("UNCHECKED_CAST")
    override fun forEach(action: BiConsumer<in K, in V>) {
        val current = snapshot
        if (current != null) {
            val ks = current.keys
            val vs = current.values
            for (i in ks.indices) {
                action.accept(ks[i] as K, vs[i] as V)
            }
            return
        }

        synchronized(this) {
            if (snapshot != null) {
                val ks = snapshot!!.keys
                val vs = snapshot!!.values
                for (i in ks.indices) {
                    action.accept(ks[i] as K, vs[i] as V)
                }
                return
            }

            val startVersion = version
            val tempKeys = ArrayList<Any?>(internalMap.size)
            val tempValues = ArrayList<Any?>(internalMap.size)

            internalMap.forEach { k, v ->
                tempKeys.add(k)
                tempValues.add(v)
                action.accept(k, v)
            }

            if (startVersion == version) {
                snapshot = Snapshot(tempKeys.toTypedArray(), tempValues.toTypedArray(), startVersion)
            }
        }
    }
}