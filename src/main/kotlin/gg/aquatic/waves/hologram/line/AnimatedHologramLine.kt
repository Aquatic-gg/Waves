package gg.aquatic.waves.hologram.line

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.PacketEntity
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.hologram.CommonHologramLineSettings
import gg.aquatic.waves.hologram.HologramLine
import gg.aquatic.waves.hologram.HologramSerializer
import gg.aquatic.waves.hologram.SpawnedHologramLine
import gg.aquatic.waves.hologram.serialize.LineFactory
import gg.aquatic.waves.hologram.serialize.LineSettings
import gg.aquatic.waves.registry.serializer.RequirementSerializer
import gg.aquatic.waves.util.collection.checkRequirements
import gg.aquatic.waves.util.getSectionList
import gg.aquatic.waves.util.requirement.ConfiguredRequirement
import gg.aquatic.waves.util.setData
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.joml.Vector3f
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AnimatedHologramLine(
    val frames: MutableList<Pair<Int, HologramLine>>,
    override val height: Double,
    override val filter: (Player) -> Boolean,
    override val failLine: HologramLine?,
    override var scale: Float,
    override var billboard: Display.Billboard,
    override var transformationDuration: Int,
    override var teleportInterpolation: Int, override var translation: Vector3f = Vector3f(),
) : HologramLine {

    val ticks = ConcurrentHashMap<UUID, AnimationHandle>()
    override fun spawn(
        location: Location,
        player: Player,
        textUpdater: (String) -> String,
    ): PacketEntity {
        return frames.first().second.spawn(location, player) { str -> textUpdater(str) }
    }

    override fun tick(spawnedHologramLine: SpawnedHologramLine) {
        val handle = ticks.getOrPut(spawnedHologramLine.player.uniqueId) { AnimationHandle() }
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
                spawnedHologramLine.packetEntity.sendDespawn(Waves.NMS_HANDLER, false, spawnedHologramLine.player)
                val packetEntity = frame.spawn(
                    spawnedHologramLine.currentLocation,
                    spawnedHologramLine.player
                ) { str -> spawnedHologramLine.textUpdater(spawnedHologramLine.player, str) }
                spawnedHologramLine.packetEntity = packetEntity
                spawnedHologramLine.packetEntity.sendSpawnComplete(Waves.NMS_HANDLER, false, spawnedHologramLine.player)
                return
            }
            val data = buildData(spawnedHologramLine)

            spawnedHologramLine.packetEntity.setData(data)
            spawnedHologramLine.packetEntity.sendDataUpdate(Waves.NMS_HANDLER, false, spawnedHologramLine.player)
            return
        }
        frame.tick(spawnedHologramLine)
    }


    override fun buildData(textUpdater: (String) -> String): List<EntityDataValue> {
        return frames.first().second.buildData(textUpdater)
    }

    override fun buildData(spawnedHologramLine: SpawnedHologramLine): List<EntityDataValue> {
        return frames[ticks.getOrPut(spawnedHologramLine.player.uniqueId) { AnimationHandle() }.index].second.buildData(
            spawnedHologramLine
        )
    }

    class AnimationHandle {
        var tick: Int = -1
        var index: Int = 0
    }

    class Settings(
        val frames: MutableList<Pair<Int, LineSettings>>,
        val height: Double,
        val conditions: List<ConfiguredRequirement<Player>>,
        val failLine: LineSettings?,
    ) : LineSettings {
        override fun create(): HologramLine {
            return AnimatedHologramLine(
                frames.map { it.first to it.second.create() }.toMutableList(),
                height,
                { p -> conditions.checkRequirements(p) },
                failLine?.create(),
                0f,
                Display.Billboard.FIXED,
                0,
                0
            )
        }
    }

    companion object : LineFactory {
        override fun load(section: ConfigurationSection, commonOptions: CommonHologramLineSettings): LineSettings? {
            val frames = ArrayList<Pair<Int, LineSettings>>()
            val height = section.getDouble("height", commonOptions.height)
            val conditions = RequirementSerializer.fromSections<Player>(section.getSectionList("view-conditions"))
            val failLine = section.getConfigurationSection("fail-line")?.let {
                HologramSerializer.loadLine(it, commonOptions)
            }
            for (configurationSection in section.getSectionList("frames")) {
                val frame = HologramSerializer.loadLine(configurationSection, commonOptions) ?: continue
                val stay = configurationSection.getInt("stay", 1)
                frames.add(stay to frame)
            }
            if (frames.isEmpty()) return null
            return Settings(
                frames,
                height,
                conditions,
                failLine
            )
        }

    }
}