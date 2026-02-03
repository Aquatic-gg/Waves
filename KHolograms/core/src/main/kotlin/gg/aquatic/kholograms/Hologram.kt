package gg.aquatic.kholograms

import gg.aquatic.common.coroutine.VirtualsCtx
import gg.aquatic.kholograms.line.TextHologramLine
import gg.aquatic.kholograms.serialize.LineSettings
import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.chunkId
import gg.aquatic.pakket.sendPacket
import gg.aquatic.pakket.trackedBy
import gg.aquatic.replace.PlaceholderContext
import gg.aquatic.snapshotmap.SuspendingSnapshotMap
import net.kyori.adventure.text.Component
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class Hologram(
    location: Location,
    val filter: suspend (Player) -> Boolean,
    val placeholderContext: () -> PlaceholderContext<Player>,
    viewDistance: Int,
    lines: Collection<HologramLine>,
) {

    var chunk: Chunk? = if (location.chunk.isLoaded) location.chunk else null
    var seat: Int? = null
        private set

    suspend fun setAsPassenger(seat: Int?) {
        val previous = this.seat
        this.seat = seat
        viewers.forEachSuspended { viewer, lines ->
            if (seat != null) {
                val ids = lines.lines.map { it.packetEntity.entityId }.toIntArray()
                val passengerPacket = Pakket.handler.createPassengersPacket(seat,ids)
                viewer.sendPacket(passengerPacket, false)
            } else if (previous != null) {
                val passengerPacket = Pakket.handler.createPassengersPacket(previous,intArrayOf())
                viewer.sendPacket(passengerPacket, false)
            }
        }
    }

    suspend fun setLines(lines: Collection<HologramLine>) {
        this.lines.clear()
        this.lines.addAll(lines)

        tickRange()
        destroyLines()
        viewers.forEachSuspended { viewer, _ ->
            showOrUpdate(viewer)
        }
    }

    fun setLineText(lineIndex: Int, text: Component) {
        val line = lines.elementAtOrNull(lineIndex) ?: return
        if (line !is TextHologramLine) return

        line.text = text
    }

    fun setTeleportInterpolation(interpolation: Int) {
        for (line in lines) {
            line.teleportInterpolation = interpolation
        }
    }

    fun setTransformationInterpolationDuration(duration: Int) {
        for (line in lines) {
            line.transformationDuration = duration
        }
    }

    fun setScale(scale: Float) {
        for (line in lines) {
            line.scale = scale
        }
    }

    var location = location
        private set

    @Volatile
    private var rangeTick = 0

    val lines = ConcurrentHashMap.newKeySet<HologramLine>().apply { addAll(lines) }
    val viewers = SuspendingSnapshotMap<Player, HologramViewer>()

    init {
        val chunkId = location.chunk.chunkId()
        if (this.chunk == null) {
            HologramHandler.waitingHolograms.getOrPut(chunkId) { ArrayList() }.add(this)
        } else {
            HologramHandler.tickingHolograms.getOrPut(chunkId) { ArrayList() }.add(this)
            VirtualsCtx {
                checkPlayersRange()
                tick()
            }
        }
    }

    suspend fun tick() {
        tickRange()
        viewers.forEachSuspended { player, _ ->
            // CurrentLineIndex -> Get Hologram line -> Compare Hologram Line with SpawnedHologramLine
            // If it is the same, then skip & add index, otherwise update line, add to the set & move other lines

            // First process all line text update & visibility and then apply changes

            showOrUpdate(player)
        }
    }

    suspend fun showOrUpdate(player: Player) {
        val viewer = viewers.getOrPut(player) { HologramViewer(player, placeholderContext(), mutableListOf()) }
        val spawnedLines = viewer.lines

        val visibleLines = ArrayList<HologramLine>(this.lines.size)
        for (line in this.lines) {
            var current: HologramLine? = line
            while (current != null) {
                if (current.filter(player)) {
                    visibleLines.add(current)
                    break
                }
                current = current.failLine
            }
        }

        // Remove spawned lines that are no longer visible
        val iterator = spawnedLines.iterator()
        while (iterator.hasNext()) {
            val spawned = iterator.next()
            if (spawned.line !in visibleLines) {
                spawned.destroy()
                iterator.remove()
            }
        }

        var height = 0.0
        var changed = false
        val currentEntityIds = IntArray(visibleLines.size)

        for ((index, line) in visibleLines.withIndex()) {
            val halfHeight = line.height / 2.0
            height += halfHeight
            val targetLocation = this.location.clone().add(0.0, height, 0.0)

            val existing = spawnedLines.find { it.line == line }
            val packetEntity = if (existing == null) {
                val entity = line.spawn(targetLocation, player, viewer.context)
                val newLine = HologramLineHandle(this, player, line, targetLocation, viewer.context, entity)
                spawnedLines.add(newLine)
                changed = true
                entity
            } else {
                existing.tick()
                if (existing.currentLocation != targetLocation) {
                    existing.move(targetLocation)
                }
                existing.packetEntity
            }

            currentEntityIds[index] = packetEntity.entityId
            height += halfHeight
        }

        // Ensure the internal list order matches the visual order for the next tick
        if (changed) {
            spawnedLines.sortBy { spawned -> visibleLines.indexOf(spawned.line) }
        }

        if ((changed || seat != null) && seat != null) {
            val passengerPacket = Pakket.handler.createPassengersPacket(seat!!, currentEntityIds)
            player.sendPacket(passengerPacket, false)
        }
    }

    private suspend fun tickRange() {
        rangeTick++
        if (rangeTick < 5) {
            return
        }
        rangeTick = 0
        checkPlayersRange()
    }

    private val viewDistanceSquared = (viewDistance * viewDistance).toDouble()

    suspend fun checkPlayersRange() {
        val remaining = viewers.toMutableMap()
        val chunk = this.chunk
        if (chunk != null) {
            for (trackedByPlayer in chunk.trackedBy()) {
                if (!filter(trackedByPlayer)) continue
                if (trackedByPlayer.world != location.world) continue
                if (trackedByPlayer.location.distanceSquared(location) <= viewDistanceSquared) {
                    remaining.remove(trackedByPlayer)
                    if (viewers.containsKey(trackedByPlayer)) {
                        continue
                    }
                    showOrUpdate(trackedByPlayer)
                }
            }
        }

        for (removed in remaining) {
            removed.value.lines.forEach { it.destroy() }
            viewers.remove(removed.key)
        }
    }

    suspend fun destroyLines() {
        viewers.forEachSuspended { _, spawnedHologramLines ->
            spawnedHologramLines.lines.forEach { it.destroy() }
            spawnedHologramLines.lines.clear()
        }
    }

    suspend fun destroy() {
        HologramHandler.removeHologram(this)
        destroyLines()
        viewers.clear()
        lines.clear()

    }

    suspend fun teleport(location: Location) {
        this.location = location
        viewers.forEachSuspended { player, _ ->
            showOrUpdate(player)
        }
    }

    class Settings(
        val lines: List<LineSettings>,
        val filter: suspend (Player) -> Boolean,
        val viewDistance: Int,
    ) {
        fun create(
            location: Location,
            placeholderContext: () -> PlaceholderContext<Player>,
            filter: (Player) -> Boolean = { true },
        ): Hologram = Hologram(
            location,
            { p ->
                filter(p) && this.filter(p)
            },
            placeholderContext,
            viewDistance,
            lines.map { it.create() }.toSet()
        )
    }

    class HologramViewer(
        val player: Player,
        val context: PlaceholderContext<Player>,
        val lines: MutableList<HologramLineHandle>
    )
}
