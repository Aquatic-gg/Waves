package gg.aquatic.waves.hologram.line

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.PacketEntity
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.fake.entity.data.impl.display.DisplayEntityData
import gg.aquatic.waves.fake.entity.data.impl.display.ItemDisplayEntityData
import gg.aquatic.waves.hologram.CommonHologramLineSettings
import gg.aquatic.waves.hologram.HologramLine
import gg.aquatic.waves.hologram.HologramSerializer
import gg.aquatic.waves.hologram.SpawnedHologramLine
import gg.aquatic.waves.hologram.serialize.LineFactory
import gg.aquatic.waves.hologram.serialize.LineSettings
import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.registry.serializer.RequirementSerializer
import gg.aquatic.waves.util.collection.checkRequirements
import gg.aquatic.waves.util.getSectionList
import gg.aquatic.waves.util.requirement.ConfiguredRequirement
import gg.aquatic.waves.util.setData
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f

class ItemHologramLine(
    val item: ItemStack,
    override var height: Double = 0.3,
    override var scale: Float = 1.0f,
    override var billboard: Billboard = Billboard.CENTER,
    val itemDisplayTransform: ItemDisplayTransform,
    override val filter: (Player) -> Boolean,
    override var failLine: HologramLine?,
    override var transformationDuration: Int,
    override var teleportInterpolation: Int,
) : HologramLine {
    override fun spawn(
        location: Location,
        player: Player,
        textUpdater: (String) -> String,
    ): PacketEntity {
        val packetEntity =
            Waves.NMS_HANDLER.createEntity(location, EntityType.ITEM_DISPLAY, null)
                ?: throw Exception("Failed to create entity")
        val entityData = buildData(textUpdater)
        packetEntity.setData(entityData)
        return packetEntity
    }

    override fun tick(spawnedHologramLine: SpawnedHologramLine) {

    }

    override fun buildData(textUpdater: (String) -> String): List<EntityDataValue> {
        val data = ArrayList<EntityDataValue>()

        data += ItemDisplayEntityData.Item.generate(item)
        data += DisplayEntityData.Billboard.generate(billboard)
        data += ItemDisplayEntityData.ItemDisplayTransform.generate(itemDisplayTransform)
        data += DisplayEntityData.Scale.generate(Vector3f(scale, scale, scale))
        data += DisplayEntityData.TransformationInterpolationDuration.generate(transformationDuration)
        data += DisplayEntityData.TeleportationDuration.generate(teleportInterpolation)

        return data
    }

    class Settings(
        val item: ItemStack,
        val height: Double = 0.3,
        val scale: Float = 1.0f,
        val billboard: Billboard = Billboard.CENTER,
        val itemDisplayTransform: ItemDisplayTransform,
        val conditions: List<ConfiguredRequirement<Player>>,
        val failLine: LineSettings?,
        val transformationDuration: Int,
        val teleportInterpolation: Int,
    ) : LineSettings {
        override fun create(): HologramLine {
            return ItemHologramLine(
                item,
                height,
                scale,
                billboard,
                itemDisplayTransform,
                { p ->
                    conditions.checkRequirements(p)
                },
                failLine?.create(),
                transformationDuration,
                teleportInterpolation,
            )
        }
    }

    companion object : LineFactory {
        override fun load(section: ConfigurationSection, commonOptions: CommonHologramLineSettings): LineSettings? {
            val item = AquaticItem.loadFromYml(section.getConfigurationSection("item")) ?: return null
            val height = section.getDouble("height", commonOptions.height)
            val scale = section.getDouble("scale", commonOptions.scale.toDouble()).toFloat()
            val billboard = section.getString("billboard")?.let {
                Billboard.valueOf(it.uppercase())
            } ?: commonOptions.billboard
            val itemDisplayTransform =
                ItemDisplayTransform.valueOf(section.getString("item-display-transform", "NONE")!!.uppercase())
            val conditions = RequirementSerializer.fromSections<Player>(section.getSectionList("view-conditions"))
            val failLine = section.getConfigurationSection("fail-line")?.let {
                HologramSerializer.loadLine(it, commonOptions)
            }
            val transformationDuration = section.getInt("transformation-duration", commonOptions.transformationDuration)
            val teleportInterpolation = section.getInt("teleport-interpolation", commonOptions.teleportInterpolation)
            return Settings(
                item.getItem(),
                height,
                scale, billboard, itemDisplayTransform, conditions, failLine, transformationDuration, teleportInterpolation
            )
        }
    }

}