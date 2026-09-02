package gg.aquatic.kholograms

import gg.aquatic.common.event
import gg.aquatic.common.ticker.GlobalTicker
import gg.aquatic.pakket.chunkId
import gg.aquatic.snapshotmap.SuspendingSnapshotMap
import org.bukkit.Chunk
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object HologramHandler {
    val tickingHolograms = SuspendingSnapshotMap<HologramChunkKey, MutableCollection<Hologram>>()
    val waitingHolograms = SuspendingSnapshotMap<HologramChunkKey, MutableCollection<Hologram>>()

    fun initialize() {
        GlobalTicker.runRepeatFixedDelay(50L) {
            tickingHolograms.forEachSuspended { _, list ->
                val iterator = list.iterator()
                while (iterator.hasNext()) {
                    val hologram = iterator.next()
                    try {
                        hologram.tick()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        event<ChunkLoadEvent> {
            val chunkId = it.chunk.hologramChunkKey()
            val toLoad = waitingHolograms.remove(chunkId) ?: return@event
            for (hologram in toLoad) {
                hologram.chunk = it.chunk
            }
            tickingHolograms.addHolograms(chunkId, toLoad)
        }
        event<ChunkUnloadEvent> {
            val chunkId = it.chunk.hologramChunkKey()
            val toWait = tickingHolograms.remove(chunkId) ?: return@event
            for (hologram in toWait) {
                hologram.chunk = null
            }
            waitingHolograms.addHolograms(chunkId, toWait)
        }
    }

    fun allHolograms(): Collection<Hologram> {
        return listOf(tickingHolograms.values.flatten(), waitingHolograms.values.flatten()).flatten()
    }

    suspend fun destroyHolograms() {
        for (hologram in allHolograms()) {
            hologram.destroy()
        }
        waitingHolograms.clear()
        tickingHolograms.clear()
    }

    suspend fun removeHologram(hologram: Hologram) {
        tickingHolograms.forEachSuspended { _, holograms ->
            holograms.remove(hologram)
        }
        waitingHolograms.forEachSuspended { _, holograms ->
            holograms.remove(hologram)
        }
    }
}

data class HologramChunkKey(
    val worldId: UUID,
    val x: Int,
    val z: Int,
)

internal fun Chunk.hologramChunkKey(): HologramChunkKey {
    val chunkId = chunkId()
    return HologramChunkKey(world.uid, chunkId.x, chunkId.z)
}

/**
 * Adds holograms under the map's per-key lock so the add cannot land on a set
 * that a concurrent remove(chunkId) already detached from the map.
 */
internal fun SuspendingSnapshotMap<HologramChunkKey, MutableCollection<Hologram>>.addHolograms(
    chunkId: HologramChunkKey,
    holograms: Collection<Hologram>,
) {
    compute(chunkId) { _, existing ->
        (existing ?: ConcurrentHashMap.newKeySet()).also { it += holograms }
    }
}
