package gg.aquatic.snapshotmap

import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiConsumer

class SnapshotMap<K : Any, V : Any>(
    internalMap: ConcurrentHashMap<K, V> = ConcurrentHashMap()
) : AbstractSnapshotMap<K, V>(internalMap) {

    override fun getOrComputeSnapshot(): Snapshot {
        val current = snapshot
        if (current != null) return current
        return synchronized(this) {
            snapshot ?: createSnapshotUnderLock()
        }
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
            val secondCheck = snapshot
            if (secondCheck != null) {
                val ks = secondCheck.keys
                val vs = secondCheck.values
                for (i in ks.indices) {
                    action.accept(ks[i] as K, vs[i] as V)
                }
                return
            }

            val startVersion = version
            val tempKeys = ArrayList<Any?>(internalMap.size)
            val tempValues = ArrayList<Any?>(internalMap.size)

            internalMap.forEach { (k, v) ->
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