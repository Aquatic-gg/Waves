package gg.aquatic.snapshotmap

import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiConsumer

class SnapshotMap<K: Any, V: Any>(
    private val internalMap: ConcurrentHashMap<K, V> = ConcurrentHashMap()
) : MutableMap<K, V> by internalMap {

    private class Snapshot(val keys: Array<Any?>, val values: Array<Any?>)

    @Volatile
    private var snapshot: Snapshot? = null

    private fun invalidate() {
        snapshot = null
    }

    override fun put(key: K, value: V): V? {
        val prev = internalMap.put(key, value)
        // Only invalidate if the value actually changed or was added
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
                // Snapshot values are guaranteed non-null in this logic
                action.accept(ks[i] as K, vs[i] as V)
            }
            return
        }

        synchronized(this) {
            val secondCheck = snapshot
            if (secondCheck != null) {
                val ks = secondCheck.keys
                val vs = secondCheck.values
                for (i in ks.indices) {
                    action.accept(ks[i] as K, vs[i] as V)
                }
            } else {
                // Use a temporary list to handle the size uncertainty of CHM
                val tempKeys = ArrayList<Any?>(internalMap.size)
                val tempValues = ArrayList<Any?>(internalMap.size)

                internalMap.forEach { k, v ->
                    tempKeys.add(k)
                    tempValues.add(v)
                    action.accept(k, v)
                }

                val snap = Snapshot(tempKeys.toTypedArray(), tempValues.toTypedArray())
                snapshot = snap
            }
        }
    }
}