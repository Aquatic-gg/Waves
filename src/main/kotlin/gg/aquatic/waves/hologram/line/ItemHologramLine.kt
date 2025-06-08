package gg.aquatic.waves.hologram.line

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.fake.entity.data.impl.display.DisplayEntityData
import gg.aquatic.waves.fake.entity.data.impl.display.ItemDisplayEntityData
import gg.aquatic.waves.hologram.*
import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.registry.serializer.RequirementSerializer
import gg.aquatic.waves.util.collection.checkRequirements
import gg.aquatic.waves.util.getSectionList
import gg.aquatic.waves.util.item.loadFromYml
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
    override val height: Double = 0.3,
    val scale: Float = 1.0f,
    val billboard: Billboard = Billboard.CENTER,
    val itemDisplayTransform: ItemDisplayTransform,
    override val filter: (Player) -> Boolean,
    override val failLine: HologramLine?,
) : HologramLine() {
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
    }

    override fun update(spawnedHologramLine: SpawnedHologramLine) {

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
            Waves.NMS_HANDLER.createEntity(spawnedHologramLine.currentLocation, EntityType.ITEM_DISPLAY, null)
                ?: throw Exception("Failed to create entity")

        spawnedHologramLine.packetEntity = packetEntity
        val entityData = buildData(spawnedHologramLine)

        packetEntity.setData(entityData)
        packetEntity.sendSpawnComplete(Waves.NMS_HANDLER, false, spawnedHologramLine.player)
    }

    override fun buildData(spawnedHologramLine: SpawnedHologramLine): List<EntityDataValue> {
        val data = ArrayList<EntityDataValue>()

        data += ItemDisplayEntityData.Item.generate(item)
        data += DisplayEntityData.Billboard.generate(billboard)
        data += ItemDisplayEntityData.ItemDisplayTransform.generate(itemDisplayTransform)
        data += DisplayEntityData.Scale.generate(Vector3f(scale, scale, scale))

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
                failLine?.create()
            )
        }
    }

    companion object : LineFactory {
        override fun load(section: ConfigurationSection): LineSettings? {
            val item = AquaticItem.loadFromYml(section.getConfigurationSection("item")) ?: return null
            val height = section.getDouble("height", 0.3)
            val scale = section.getDouble("scale", 1.0).toFloat()
            val billboard = Billboard.valueOf(section.getString("billboard", "CENTER")!!.uppercase())
            val itemDisplayTransform =
                ItemDisplayTransform.valueOf(section.getString("item-display-transform", "NONE")!!.uppercase())
            val conditions = RequirementSerializer.fromSections<Player>(section.getSectionList("view-conditions"))
            val failLine = section.getConfigurationSection("fail-line")?.let {
                HologramSerializer.loadLine(it)
            }
            return Settings(
                item.getItem(),
                height,
                scale, billboard, itemDisplayTransform, conditions, failLine
            )
        }
    }

}