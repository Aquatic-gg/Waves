package gg.aquatic.waves.hologram.line

import gg.aquatic.waves.Waves
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.hologram.*
import gg.aquatic.waves.registry.serializer.RequirementSerializer
import gg.aquatic.waves.util.collection.checkRequirements
import gg.aquatic.waves.util.getSectionList
import gg.aquatic.waves.util.modify
import gg.aquatic.waves.util.requirement.ConfiguredRequirement
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AnimatedHologramLine(
    val frames: MutableList<Pair<Int, HologramLine>>,
    override val height: Double,
    override val filter: (Player) -> Boolean,
    override val failLine: HologramLine?
) : HologramLine() {

    val ticks = ConcurrentHashMap<UUID, AnimationHandle>()

    override fun spawn(
        location: Location,
        player: Player,
        textUpdater: (Player, String) -> String
    ): SpawnedHologramLine {
        val spawned = SpawnedHologramLine(
            player,
            this,
            location,
            textUpdater
        )

        createEntity(spawned)

        return spawned
    }

    override fun destroy(spawnedHologramLine: SpawnedHologramLine) {
        spawnedHologramLine.packetEntity.sendDespawn(Waves.NMS_HANDLER, false, spawnedHologramLine.player)
        ticks.remove(spawnedHologramLine.player.uniqueId)
    }

    override fun update(spawnedHologramLine: SpawnedHologramLine) {
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
                frame.createEntity(spawnedHologramLine)
                return
            }
            val data = buildData(spawnedHologramLine)
            spawnedHologramLine.packetEntity.modify {
                for (entityData in data) {
                    entityData.apply(it)
                }
            }
            spawnedHologramLine.packetEntity.sendDataUpdate(Waves.NMS_HANDLER, false, spawnedHologramLine.player)
            return
        }
        frame.update(spawnedHologramLine)
    }

    override fun move(spawnedHologramLine: SpawnedHologramLine) {
        spawnedHologramLine.packetEntity.teleport(
            Waves.NMS_HANDLER,
            spawnedHologramLine.currentLocation,
            false,
            spawnedHologramLine.player
        )
    }

    override fun createEntity(spawnedHologramLine: SpawnedHologramLine) {
        val handle = ticks.getOrPut(spawnedHologramLine.player.uniqueId) { AnimationHandle() }
        val frame = frames[handle.index].second

        frame.createEntity(spawnedHologramLine)
    }

    override fun buildData(spawnedHologramLine: SpawnedHologramLine): List<EntityData> {
        return frames[ticks.getOrPut(spawnedHologramLine.player.uniqueId) { AnimationHandle() }.index].second.buildData(spawnedHologramLine)
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
                failLine?.create()
            )
        }
    }

    companion object : LineFactory {
        override fun load(section: ConfigurationSection): Settings? {
            val frames = ArrayList<Pair<Int, LineSettings>>()
            val height = section.getDouble("height", 0.5)
            val conditions = RequirementSerializer.fromSections<Player>(section.getSectionList("view-conditions"))
            val failLine = section.getConfigurationSection("fail-line")?.let {
                HologramSerializer.loadLine(it)
            }
            for (configurationSection in section.getSectionList("frames")) {
                val frame = HologramSerializer.loadLine(configurationSection) ?: continue
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