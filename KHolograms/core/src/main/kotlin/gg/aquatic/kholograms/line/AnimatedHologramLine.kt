package gg.aquatic.kholograms.line

import gg.aquatic.execute.checkConditions
import gg.aquatic.execute.requirement.ConditionHandle
import gg.aquatic.kholograms.HologramLine
import gg.aquatic.kholograms.HologramLineHandle
import gg.aquatic.kholograms.serialize.LineSettings
import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.api.nms.PacketEntity
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.sendPacket
import gg.aquatic.replace.placeholder.PlaceholderContext
import gg.aquatic.snapshotmap.SnapshotMap
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.joml.Vector3f
import java.util.*

class AnimatedHologramLine(
    val frames: MutableList<Pair<Int, HologramLine>>,
    override val height: Double,
    override val filter: suspend (Player) -> Boolean,
    override val failLine: HologramLine?,
    override var scale: Float,
    override var billboard: Display.Billboard,
    override var transformationDuration: Int,
    override var teleportInterpolation: Int, override var translation: Vector3f = Vector3f(),
) : HologramLine {

    val ticks = SnapshotMap<UUID, AnimationHandle>()
    override fun spawn(
        location: Location,
        player: Player,
        placeholderContext: PlaceholderContext<Player>,
    ): PacketEntity {
        return frames.first().second.spawn(location, player, placeholderContext)
    }

    override suspend fun tick(hologramLineHandle: HologramLineHandle) {
        val handle = ticks.getOrPut(hologramLineHandle.player.uniqueId) { AnimationHandle() }
        handle.tick++

        var (stay, frame) = frames[handle.index]
        if (handle.tick >= stay) {
            handle.tick = 0
            handle.index++
            if (handle.index >= frames.size) {
                handle.index = 0
            }
            val pair = frames[handle.index]
            val previousFrame = frame
            frame = pair.second

            if (previousFrame.javaClass != frame.javaClass) {
                hologramLineHandle.packetEntity.sendDespawn(Pakket.handler, false, hologramLineHandle.player)
                val packetEntity = frame.spawn(
                    hologramLineHandle.currentLocation,
                    hologramLineHandle.player,
                    hologramLineHandle.placeholderContext
                )
                hologramLineHandle.packetEntity = packetEntity
                hologramLineHandle.packetEntity.sendSpawnComplete(Pakket.handler, false, hologramLineHandle.player)
                return
            }
            val data = buildData(hologramLineHandle)
            if (data.isEmpty()) return

            frame.tick(hologramLineHandle)

            val packet = Pakket.handler.createEntityUpdatePacket(hologramLineHandle.packetEntity.entityId, data)
            hologramLineHandle.packetEntity.updatePacket = packet

            hologramLineHandle.player.sendPacket(packet)
            return
        }
    }

    override fun buildData(
        placeholderContext: PlaceholderContext<Player>,
        player: Player
    ): List<EntityDataValue> {
        return frames.first().second.buildData(placeholderContext, player)
    }

    override fun buildData(hologramLineHandle: HologramLineHandle): List<EntityDataValue> {
        return frames[ticks.getOrPut(hologramLineHandle.player.uniqueId) { AnimationHandle() }.index].second.buildData(
            hologramLineHandle
        )
    }

    class AnimationHandle {
        var tick: Int = -1
        var index: Int = 0
    }

    class Settings(
        val frames: MutableList<Pair<Int, LineSettings>>,
        val height: Double,
        val conditions: List<ConditionHandle<Player>>,
        val failLine: LineSettings?,
    ) : LineSettings {
        override fun create(): HologramLine {
            return AnimatedHologramLine(
                frames.map { it.first to it.second.create() }.toMutableList(),
                height,
                { p -> conditions.checkConditions(p) },
                failLine?.create(),
                0f,
                Display.Billboard.FIXED,
                0,
                0
            )
        }
    }

}
