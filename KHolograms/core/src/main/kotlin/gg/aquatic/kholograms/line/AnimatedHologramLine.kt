package gg.aquatic.kholograms.line

import gg.aquatic.kholograms.HologramLine
import gg.aquatic.kholograms.HologramLineHandle
import gg.aquatic.kholograms.serialize.LineSettings
import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.api.nms.PacketEntity
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.sendPacket
import gg.aquatic.replace.PlaceholderContext
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
            frame = pair.second
            val renderedLine = hologramLineHandle.renderedLine.takeIf { it !== this }
                ?: createRenderState(frame).also { hologramLineHandle.renderedLine = it }
            if (tryApplyFrame(renderedLine, frame)) {
                val data = renderedLine.buildData(
                    hologramLineHandle.placeholderContext,
                    hologramLineHandle.player
                )
                if (data.isNotEmpty()) {
                    val packet = Pakket.handler.createEntityUpdatePacket(hologramLineHandle.packetEntity.entityId, data)
                    hologramLineHandle.packetEntity.updatePacket = packet
                    hologramLineHandle.player.sendPacket(packet, false)
                }
                return
            }

            hologramLineHandle.packetEntity.sendDespawn(Pakket.handler, false, hologramLineHandle.player)
            val newRenderState = createRenderState(frame)
            hologramLineHandle.renderedLine = newRenderState
            val packetEntity = newRenderState.spawn(
                hologramLineHandle.currentLocation,
                hologramLineHandle.player,
                hologramLineHandle.placeholderContext
            )
            hologramLineHandle.packetEntity = packetEntity
            hologramLineHandle.packetEntity.sendSpawnComplete(Pakket.handler, false, hologramLineHandle.player)
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
        val handle = ticks.getOrPut(hologramLineHandle.player.uniqueId) { AnimationHandle() }
        val renderedLine = hologramLineHandle.renderedLine.takeIf { it !== this }
            ?: createRenderState(frames[handle.index].second).also {
                hologramLineHandle.renderedLine = it
            }
        return renderedLine.buildData(hologramLineHandle)
    }

    class AnimationHandle {
        var tick: Int = -1
        var index: Int = 0
    }

    class Settings(
        val frames: MutableList<Pair<Int, LineSettings>>,
        val height: Double,
        val filter: suspend (Player) -> Boolean,
        val failLine: LineSettings?,
    ) : LineSettings {
        override fun create(): HologramLine {
            return AnimatedHologramLine(
                frames.map { it.first to it.second.create() }.toMutableList(),
                height,
                filter,
                failLine?.create(),
                0f,
                Display.Billboard.FIXED,
                0,
                0
            )
        }
    }

    private fun createRenderState(line: HologramLine): HologramLine = when (line) {
        is ItemHologramLine -> ItemHologramLine(
            item = line.item.clone(),
            height = line.height,
            scale = line.scale,
            billboard = line.billboard,
            itemDisplayTransform = line.itemDisplayTransform,
            filter = line.filter,
            failLine = line.failLine,
            transformationDuration = line.transformationDuration,
            teleportInterpolation = line.teleportInterpolation,
            translation = Vector3f(line.translation)
        )

        is TextHologramLine -> TextHologramLine(
            height = line.height,
            filter = line.filter,
            failLine = line.failLine,
            text = line.text,
            lineWidth = line.lineWidth,
            scale = line.scale,
            billboard = line.billboard,
            hasShadow = line.hasShadow,
            backgroundColor = line.backgroundColor,
            isSeeThrough = line.isSeeThrough,
            transformationDuration = line.transformationDuration,
            teleportInterpolation = line.teleportInterpolation,
            translation = Vector3f(line.translation)
        )

        else -> line
    }

    private fun tryApplyFrame(renderedLine: HologramLine, targetFrame: HologramLine): Boolean {
        return when {
            renderedLine is ItemHologramLine && targetFrame is ItemHologramLine -> {
                renderedLine.item = targetFrame.item.clone()
                renderedLine.itemDisplayTransform = targetFrame.itemDisplayTransform
                renderedLine.scale = targetFrame.scale
                renderedLine.billboard = targetFrame.billboard
                renderedLine.transformationDuration = targetFrame.transformationDuration
                renderedLine.teleportInterpolation = targetFrame.teleportInterpolation
                renderedLine.translation = Vector3f(targetFrame.translation)
                true
            }

            renderedLine is TextHologramLine && targetFrame is TextHologramLine -> {
                renderedLine.text = targetFrame.text
                renderedLine.lineWidth = targetFrame.lineWidth
                renderedLine.scale = targetFrame.scale
                renderedLine.billboard = targetFrame.billboard
                renderedLine.hasShadow = targetFrame.hasShadow
                renderedLine.backgroundColor = targetFrame.backgroundColor
                renderedLine.isSeeThrough = targetFrame.isSeeThrough
                renderedLine.transformationDuration = targetFrame.transformationDuration
                renderedLine.teleportInterpolation = targetFrame.teleportInterpolation
                renderedLine.translation = Vector3f(targetFrame.translation)
                true
            }

            else -> false
        }
    }
}
