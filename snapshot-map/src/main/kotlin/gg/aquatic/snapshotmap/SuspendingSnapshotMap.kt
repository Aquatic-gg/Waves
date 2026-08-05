package gg.aquatic.snapshotmap

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class SuspendingSnapshotMap<K : Any, V : Any>(
    internalMap: ConcurrentHashMap<K, V> = ConcurrentHashMap()
) : AbstractSnapshotMap<K, V>(internalMap) {

    @PublishedApi
    internal val mutex = Mutex()

    /**
     * Bridges the non-suspending Map API (keys, values, etc.) to the Mutex.
     * Since we can't suspend here, we use a loop with tryLock.
     */
    override fun getOrComputeSnapshot(): Snapshot {
        val current = snapshot
        if (current != null) return current

        // Spin-wait/Busy-wait on the mutex.
        // In a SnapshotMap, snapshot creation is fast (just array copying),
        // so this is more efficient than runBlocking.
        while (true) {
            val s = snapshot
            if (s != null) return s

            if (mutex.tryLock()) {
                try {
                    snapshot?.let { return it }
                    return createSnapshotUnderLock()
                } finally {
                    mutex.unlock()
                }
            }
            Thread.onSpinWait()
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend inline fun forEachSuspended(crossinline action: suspend (K, V) -> Unit) {
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