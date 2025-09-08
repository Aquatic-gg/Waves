package gg.aquatic.waves.hologram.line

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.PacketEntity
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.fake.entity.data.impl.display.DisplayEntityData
import gg.aquatic.waves.fake.entity.data.impl.display.TextDisplayEntityData
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
import gg.aquatic.waves.util.toMMComponent
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.joml.Vector3f

class TextHologramLine(
    override var height: Double,
    override var filter: (Player) -> Boolean,
    override var failLine: HologramLine?,
    var text: String,
    var lineWidth: Int,
    override var scale: Float = 1.0f,
    override var billboard: Billboard = Billboard.CENTER,
    var hasShadow: Boolean = true,
    var backgroundColor: Color? = null,
    var isSeeThrough: Boolean = true,
    override var transformationDuration: Int = 0,
    override var teleportInterpolation: Int,
) : HologramLine {
    override fun spawn(
        location: Location,
        player: Player,
        textUpdater: (String) -> String,
    ): PacketEntity {
        val packetEntity =
            Waves.NMS_HANDLER.createEntity(location, EntityType.TEXT_DISPLAY, null)
                ?: throw Exception("Failed to create entity")
        val entityData = buildData(textUpdater)
        packetEntity.setData(entityData)
        return packetEntity
    }

    override fun tick(spawnedHologramLine: SpawnedHologramLine) {
        val data = buildData(spawnedHologramLine)
        spawnedHologramLine.packetEntity.setData(data)
        spawnedHologramLine.packetEntity.sendDataUpdate(Waves.NMS_HANDLER, false, spawnedHologramLine.player)
    }

    override fun buildData(textUpdater: (String) -> String): List<EntityDataValue> {
        val updatedText = textUpdater(text)

        val data = ArrayList<EntityDataValue>()

        data += DisplayEntityData.InterpolationDelay.generate(0)
        data += DisplayEntityData.TransformationInterpolationDuration.generate(transformationDuration)
        data += DisplayEntityData.TeleportationDuration.generate(teleportInterpolation)
        data += TextDisplayEntityData.Text.generate(updatedText.toMMComponent())
        data += TextDisplayEntityData.Width.generate(lineWidth)
        data += DisplayEntityData.Billboard.generate(billboard)
        data += TextDisplayEntityData.Flags.generate(hasShadow, isSeeThrough, backgroundColor == null)
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
        val backgroundColor: Color?,
        val isSeeThrough: Boolean,
        val transformationDuration: Int,
        val failLine: LineSettings?,
        val teleportInterpolation: Int,
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
                backgroundColor,
                isSeeThrough,
                transformationDuration,
                teleportInterpolation,
            )
        }
    }
    companion object : LineFactory {
        override fun load(section: ConfigurationSection, commonOptions: CommonHologramLineSettings): LineSettings? {
            val text = section.getString("text") ?: return null
            val height = section.getDouble("height", commonOptions.height)
            val lineWidth = section.getInt("line-width", 100)
            val scale = section.getDouble("scale", commonOptions.scale.toDouble()).toFloat()
            val billboard = section.getString("billboard")?.let {
                Billboard.valueOf(it.uppercase())
            } ?: commonOptions.billboard
            val conditions = RequirementSerializer.fromSections<Player>(section.getSectionList("view-conditions"))
            val failLine = section.getConfigurationSection("fail-line")?.let {
                HologramSerializer.loadLine(it, commonOptions)
            }
            val hasShadow = section.getBoolean("has-shadow", false)
            val backgroundColorStr = section.getString("background-color")
            val isSeeThrough = section.getBoolean("is-see-through", true)
            val transformationDuration = section.getInt("transformation-duration", commonOptions.transformationDuration)
            val backgroundColor = if (backgroundColorStr != null) {
                val args = backgroundColorStr.split(";").map { it.toIntOrNull() ?: 0 }
                Color.fromARGB(args.getOrNull(3) ?: 255, args[0], args[1], args[2])
            } else null
            val teleportInterpolation = section.getInt("teleport-interpolation", commonOptions.teleportInterpolation)

            return Settings(
                height,
                text,
                lineWidth,
                scale,
                billboard,
                conditions,
                hasShadow,
                backgroundColor,
                isSeeThrough,
                transformationDuration,
                failLine,
                teleportInterpolation
            )
        }
    }
}