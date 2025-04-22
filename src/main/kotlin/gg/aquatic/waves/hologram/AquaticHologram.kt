package gg.aquatic.waves.hologram

import gg.aquatic.waves.util.collection.checkRequirements
import gg.aquatic.waves.util.requirement.ConfiguredRequirement
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class AquaticHologram(
    location: Location,
    val filter: (Player) -> Boolean,
    val textUpdater: (Player, String) -> String,
    val viewDistance: Int,
    lines: Set<HologramLine>
) {

    var location = location
        private set

    @Volatile
    private var rangeTick = 0

    init {
        HologramHandler.spawnedHolograms += this
    }

    val lines = ConcurrentHashMap.newKeySet<HologramLine>().apply { addAll(lines) }
    val viewers = ConcurrentHashMap<Player, MutableSet<SpawnedHologramLine>>()

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
                val newLine = line.spawn(location, player, textUpdater)
                lines.add(newLine)
            } else {
                nullableSpawnedLine.update()
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
        val remaining = viewers.toMutableMap()
        for (trackedByPlayer in location.chunk.playersSeeingChunk) {
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

    fun destroy() {
        HologramHandler.spawnedHolograms -= this
        viewers.forEach { (_, spawnedHologramLines) ->
            spawnedHologramLines.forEach { it.destroy() }
        }
        viewers.clear()
        lines.clear()

    }

    fun teleport(location: Location) {
        this.location = location
        viewers.forEach { (player, _) ->
            showOrUpdate(player)
        }
    }

    class Settings(
        val lines: Set<LineSettings>,
        val conditions: List<ConfiguredRequirement<Player>>,
        val viewDistance: Int
    ) {

        fun create(location: Location, textUpdater: (Player, String) -> String): AquaticHologram = AquaticHologram(
            location,
            { p ->
                conditions.checkRequirements(p)
            },
            textUpdater,
            viewDistance,
            lines.map { it.create() }.toSet()
        )
    }

}