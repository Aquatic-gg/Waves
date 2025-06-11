package gg.aquatic.waves.hologram.line

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.fake.entity.data.impl.display.DisplayEntityData
import gg.aquatic.waves.fake.entity.data.impl.display.TextDisplayEntityData
import gg.aquatic.waves.hologram.*
import gg.aquatic.waves.registry.serializer.RequirementSerializer
import gg.aquatic.waves.util.collection.checkRequirements
import gg.aquatic.waves.util.getSectionList
import gg.aquatic.waves.util.modify
import gg.aquatic.waves.util.requirement.ConfiguredRequirement
import gg.aquatic.waves.util.setData
import gg.aquatic.waves.util.toMMComponent
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

class TextHologramLine(
    override val height: Double,
    override val filter: (Player) -> Boolean,
    override val failLine: HologramLine?,
    val text: String,
    val lineWidth: Int,
    val scale: Float = 1.0f,
    val billboard: Billboard = Billboard.CENTER,
    val hasShadow: Boolean = true,
    val defaultBackground: Boolean = true,
    val backgroundColor: org.bukkit.Color? = null,
    val isSeeThrough: Boolean = true,
    val transformationDuration: Int = 0,
) : HologramLine() {
    override fun spawn(
        location: Location,
        player: Player,
        textUpdater: (Player, String) -> String,
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
    }

    override fun update(spawnedHologramLine: SpawnedHologramLine) {
        val data = buildData(spawnedHologramLine)

        spawnedHologramLine.packetEntity.setData(data)
        spawnedHologramLine.packetEntity.sendDataUpdate(Waves.NMS_HANDLER, false, spawnedHologramLine.player)
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
        val packetEntity =
            Waves.NMS_HANDLER.createEntity(spawnedHologramLine.currentLocation, EntityType.TEXT_DISPLAY, null)
                ?: throw Exception("Failed to create entity")
        spawnedHologramLine.packetEntity = packetEntity
        val entityData = buildData(spawnedHologramLine)

        packetEntity.setData(entityData)
        packetEntity.sendSpawnComplete(Waves.NMS_HANDLER, false, spawnedHologramLine.player)
    }

    override fun buildData(spawnedHologramLine: SpawnedHologramLine): List<EntityDataValue> {
        val data = ArrayList<EntityDataValue>()

        data += DisplayEntityData.InterpolationDelay.generate(0)
        data += DisplayEntityData.TransformationInterpolationDuration.generate(transformationDuration)
        data += DisplayEntityData.TeleportationDuration.generate(transformationDuration)
        data += TextDisplayEntityData.Text.generate(
            spawnedHologramLine.textUpdater(spawnedHologramLine.player, text).toMMComponent()
        )
        data += TextDisplayEntityData.Width.generate(lineWidth)
        data += DisplayEntityData.Billboard.generate(billboard)
        data += TextDisplayEntityData.Flags.generate(hasShadow, isSeeThrough, defaultBackground)
        backgroundColor?.let {
            data += TextDisplayEntityData.BackgroundColor.generate(it)
        }
        data += DisplayEntityData.Scale.generate(Vector3f(scale, scale, scale))

        return data
    }

    class Settings(
        val height: Double,
        val text: String,
        val lineWidth: Int,
        val scale: Float = 1.0f,
        val billboard: Billboard = Billboard.CENTER,
        val conditions: List<ConfiguredRequirement<Player>>,
        val hasShadow: Boolean,
        val defaultBackground: Boolean,
        val backgroundColor: org.bukkit.Color?,
        val isSeeThrough: Boolean,
        val transformationDuration: Int,
        val failLine: LineSettings?,
    ) : LineSettings {
        override fun create(): HologramLine {
            return TextHologramLine(
                height,
                { p ->
                    conditions.checkRequirements(p)
                },
                failLine?.create(),
                text,
                lineWidth,
                scale,
                billboard,
                hasShadow,
                defaultBackground,
                backgroundColor,
                isSeeThrough,
                transformationDuration,
            )
        }
    }

    companion object : LineFactory {
        override fun load(section: ConfigurationSection): LineSettings? {
            val text = section.getString("text") ?: return null
            val height = section.getDouble("height", 0.5)
            val lineWidth = section.getInt("line-width", 100)
            val scale = section.getDouble("scale", 1.0).toFloat()
            val billboard = Billboard.valueOf(section.getString("billboard", "CENTER")!!.uppercase())
            val conditions = RequirementSerializer.fromSections<Player>(section.getSectionList("view-conditions"))
            val failLine = section.getConfigurationSection("fail-line")?.let {
                HologramSerializer.loadLine(it)
            }
            val hasShadow = section.getBoolean("has-shadow", false)
            val defaultBackground = section.getBoolean("default-background", true)
            val backgroundColorStr = section.getString("background-color")
            val isSeeThrough = section.getBoolean("is-see-through", true)
            val transformationDuration = section.getInt("transformation-duration", 0)
            val backgroundColor = if (backgroundColorStr != null) {
                val args = backgroundColorStr.split(";").map { it.toIntOrNull() ?: 0 }
                org.bukkit.Color.fromARGB(args.getOrNull(3) ?: 255, args[0], args[1], args[2])
            } else null
            return Settings(
                height,
                text,
                lineWidth,
                scale,
                billboard,
                conditions,
                hasShadow,
                defaultBackground,
                backgroundColor,
                isSeeThrough,
                transformationDuration,
                failLine,
            )
        }
    }
}