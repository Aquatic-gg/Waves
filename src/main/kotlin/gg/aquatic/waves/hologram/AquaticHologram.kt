package gg.aquatic.waves.hologram

import gg.aquatic.waves.Waves
import gg.aquatic.waves.chunk.trackedBy
import gg.aquatic.waves.hologram.serialize.LineSettings
import gg.aquatic.waves.util.collection.checkRequirements
import gg.aquatic.waves.util.requirement.ConfiguredRequirement
import gg.aquatic.waves.util.setData
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class AquaticHologram(
    location: Location,
    val filter: (Player) -> Boolean,
    val textUpdater: (Player, String) -> String,
    val viewDistance: Int,
    lines: List<HologramLine>,
) {

    var seat: Int? = null
        private set

    fun setAsPassenger(seat: Int?) {
        this.seat = seat
        viewers.forEach { (_, lines) ->
            for (line in lines) {
                line.setAsPassenger(seat)
            }
        }
    }

    fun setLines(lines: Collection<HologramLine>) {
        this.lines.clear()
        this.lines.addAll(lines)

        tickRange()
        destroyLines()
        for (player in viewers.keys) {
            showOrUpdate(player)
        }
    }

    fun setLineText(lineIndex: Int, text: String) {
        val line = lines.elementAtOrNull(lineIndex) ?: return
        if (line !is gg.aquatic.waves.hologram.line.TextHologramLine) return

        line.text = text
        for ((_, lines) in viewers) {
            for (hologramLine in lines) {
                if (hologramLine.line != line) continue
                hologramLine.tick()
            }
        }
    }

    fun setTeleportInterpolation(interpolation: Int) {
        for (line in lines) {
            line.teleportInterpolation = interpolation
        }
        sendUpdatePackets()
    }

    fun setTransformationInterpolationDuration(duration: Int) {
        for (line in lines) {
            line.transformationDuration = duration
        }
        sendUpdatePackets()
    }

    fun setScale(scale: Float) {
        for (line in lines) {
            line.scale = scale
        }
        sendUpdatePackets()
    }

    private fun sendUpdatePackets() {
        for ((_, lines) in viewers) {
            for (hologramLine in lines) {
                val data = hologramLine.line.buildData(hologramLine)
                hologramLine.packetEntity.setData(data)
                hologramLine.packetEntity.sendDataUpdate(Waves.NMS_HANDLER, false, hologramLine.player)
            }
        }
    }

    var location = location
        private set

    @Volatile
    private var rangeTick = 0

    val lines = Collections.synchronizedList(lines.reversed())
    val viewers = ConcurrentHashMap<Player, MutableSet<SpawnedHologramLine>>()

    init {
        HologramHandler.spawnedHolograms += this
        checkPlayersRange()
        tick()
    }

    fun tick() {
        tickRange()
        viewers.forEach { (player, _) ->
            // CurrentLineIndex -> Get Hologram line -> Compare Hologram Line with SpawnedHologramLine
            // If it is the same, then skip & add index, otherwise update line, add to the set & move other lines

            // First process all line text update & visibility and then apply changes

            showOrUpdate(player)
        }
    }

    fun showOrUpdate(player: Player) {
        val lines = viewers.getOrPut(player) { ConcurrentHashMap.newKeySet() }

        fun getVisibleLine(player: Player, hologramLine: HologramLine): HologramLine? {
            if (hologramLine.filter(player)) {
                return hologramLine
            }
            return getVisibleLine(player, hologramLine.failLine ?: return null)
        }

        val remainingLines = lines.toMutableSet()
        val newLines = mutableMapOf<HologramLine, SpawnedHologramLine?>()
        for (line in this.lines) {
            val visibleLine = getVisibleLine(player, line) ?: continue
            val spawnedLine = lines.find { it.line == visibleLine }
            newLines[visibleLine] = spawnedLine
            remainingLines.remove(spawnedLine ?: continue)
        }

        for (remainingLine in remainingLines) {
            remainingLine.destroy()
            lines.remove(remainingLine)
        }

        var height = 0.0
        for ((line, nullableSpawnedLine) in newLines) {
            val halfHeight = line.height / 2.0
            height += halfHeight
            val location = this.location.clone().add(0.0, height, 0.0)
            if (nullableSpawnedLine == null) {
                val packetEntity = line.spawn(location, player) { str -> textUpdater(player, str) }
                val newLine = SpawnedHologramLine(this, player, line, location, textUpdater, packetEntity)
                lines.add(newLine)
            } else {
                nullableSpawnedLine.tick()
                if (nullableSpawnedLine.currentLocation == location) continue
                nullableSpawnedLine.move(location)
            }
        }
    }

    private fun tickRange() {
        rangeTick++
        if (rangeTick < 5) {
            return
        }
        rangeTick = 0
        checkPlayersRange()
    }

    fun checkPlayersRange() {
        val remaining = viewers.toMutableMap()
        for (trackedByPlayer in location.chunk.trackedBy()) {
            if (!filter(trackedByPlayer)) continue
            if (trackedByPlayer.world != location.world) continue
            if (trackedByPlayer.location.distanceSquared(location) <= viewDistance * viewDistance) {
                remaining.remove(trackedByPlayer)
                if (viewers.containsKey(trackedByPlayer)) {
                    continue
                }
                showOrUpdate(trackedByPlayer)
            }
        }
        for (removed in remaining) {
            removed.value.forEach { it.destroy() }
            viewers.remove(removed.key)
        }
    }

    fun destroyLines() {
        viewers.forEach { (_, spawnedHologramLines) ->
            spawnedHologramLines.forEach { it.destroy() }
            spawnedHologramLines.clear()
        }
    }

    fun destroy() {
        HologramHandler.spawnedHolograms -= this
        destroyLines()
        viewers.clear()
    }

    fun teleport(location: Location) {
        this.location = location
        viewers.forEach { (player, _) ->
            showOrUpdate(player)
        }
    }

    class Settings(
        val lines: List<LineSettings>,
        val conditions: List<ConfiguredRequirement<Player>>,
        val viewDistance: Int,
    ) {
        fun create(
            location: Location,
            textUpdater: (Player, String) -> String,
            filter: (Player) -> Boolean = { true },
        ): AquaticHologram = AquaticHologram(
            location,
            { p ->
                filter(p) && conditions.checkRequirements(p)
            },
            textUpdater,
            viewDistance,
            lines.map { it.create() }
        )
    }
}