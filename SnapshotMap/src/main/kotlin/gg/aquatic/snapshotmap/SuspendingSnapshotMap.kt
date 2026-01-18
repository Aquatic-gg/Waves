package gg.aquatic.snapshotmap

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class SuspendingSnapshotMap<K : Any, V : Any>(
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

    @PublishedApi
    internal val mutex = Mutex()

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
    suspend inline fun forEachSuspended(crossinline action: (K, V) -> Unit) {
        val current = snapshot
        if (current != null) {
            val ks = current.keys
            val vs = current.values
            for (i in ks.indices) {
                action(ks[i] as K, vs[i] as V)
            }
            return
        }

        mutex.withLock {
            val secondCheck = snapshot
            if (secondCheck != null) {
                val ks = secondCheck.keys
                val vs = secondCheck.values
                for (i in ks.indices) {
                    action(ks[i] as K, vs[i] as V)
                }
                return@withLock
            }

            val startVersion = version
            val tempKeys = ArrayList<Any?>(internalMap.size)
            val tempValues = ArrayList<Any?>(internalMap.size)

            internalMap.forEach { (k, v) ->
                tempKeys.add(k)
                tempValues.add(v)
                action(k, v)
            }

            if (startVersion == version) {
                snapshot = Snapshot(tempKeys.toTypedArray(), tempValues.toTypedArray(), startVersion)
            }
        }
    }
}
