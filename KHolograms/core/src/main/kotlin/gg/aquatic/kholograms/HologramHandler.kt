package gg.aquatic.kholograms

import gg.aquatic.common.ChunkId
import gg.aquatic.common.event
import gg.aquatic.common.ticker.GlobalTicker
import gg.aquatic.pakket.chunkId
import gg.aquatic.snapshotmap.SuspendingSnapshotMap
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

object HologramHandler {
    val tickingHolograms = SuspendingSnapshotMap<ChunkId, MutableCollection<Hologram>>()
    val waitingHolograms = SuspendingSnapshotMap<ChunkId, MutableCollection<Hologram>>()

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
            val chunkId = it.chunk.chunkId()
            val toLoad = waitingHolograms.remove(chunkId) ?: return@event
            val list = tickingHolograms.getOrPut(chunkId) { ArrayList() }
            for (hologram in toLoad) {
                hologram.chunk = it.chunk
                list += hologram
            }
        }
        event<ChunkUnloadEvent> {
            val chunkId = it.chunk.chunkId()
            val toWait = tickingHolograms.remove(chunkId) ?: return@event
            val list = waitingHolograms.getOrPut(chunkId) { ArrayList() }
            for (hologram in toWait) {
                hologram.chunk = null
                list += hologram
            }
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