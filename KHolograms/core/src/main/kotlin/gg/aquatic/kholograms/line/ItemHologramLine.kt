package gg.aquatic.kholograms.line

import gg.aquatic.execute.checkConditions
import gg.aquatic.execute.requirement.ConditionHandle
import gg.aquatic.kholograms.HologramLine
import gg.aquatic.kholograms.HologramLineHandle
import gg.aquatic.kholograms.serialize.LineSettings
import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.api.nms.PacketEntity
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.api.nms.entity.data.impl.display.DisplayEntityData
import gg.aquatic.pakket.api.nms.entity.data.impl.display.ItemDisplayEntityData
import gg.aquatic.pakket.sendPacket
import gg.aquatic.replace.PlaceholderContext
import org.bukkit.Location
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f
import kotlin.properties.Delegates

class ItemHologramLine(
    item: ItemStack,
    override var height: Double = 0.3,
    scale: Float = 1.0f,
    billboard: Billboard = Billboard.CENTER,
    itemDisplayTransform: ItemDisplayTransform,
    override val filter: suspend (Player) -> Boolean,
    override var failLine: HologramLine?,
    override var transformationDuration: Int,
    override var teleportInterpolation: Int, override var translation: Vector3f,
) : HologramLine {

    private val cachedData = HashMap<Int, EntityDataValue>()

    override var billboard: Billboard by Delegates.observable(billboard) { _, old, new ->
        if (old == new) return@observable
        cacheData(DisplayEntityData.Billboard.generate(new))
    }
    override var scale: Float by Delegates.observable(scale) { _, old, new ->
        if (old == new) return@observable
        cacheData(DisplayEntityData.Scale.generate(Vector3f(scale, scale, scale)))
    }

    var item: ItemStack by Delegates.observable(item) { _, old, new ->
        if (old == new) return@observable
        cacheData(ItemDisplayEntityData.Item.generate(new))
    }

    var itemDisplayTransform: ItemDisplayTransform by Delegates.observable(itemDisplayTransform) { _, old, new ->
        if (old == new) return@observable
        cacheData(ItemDisplayEntityData.ItemDisplayTransform.generate(new))
    }

    private fun createInitialData(): List<EntityDataValue> {
        return listOf(
            ItemDisplayEntityData.Item.generate(item),
            DisplayEntityData.Billboard.generate(billboard),
            ItemDisplayEntityData.ItemDisplayTransform.generate(itemDisplayTransform),
            DisplayEntityData.Scale.generate(Vector3f(scale, scale, scale)),
            DisplayEntityData.TeleportationDuration.generate(teleportInterpolation),
            DisplayEntityData.TransformationInterpolationDuration.generate(transformationDuration)
        ).flatten()
    }

    private fun cacheData(data: Iterable<EntityDataValue>) {
        for (value in data) {
            cachedData[value.id] = value
        }
    }

    override fun spawn(
        location: Location,
        player: Player,
        placeholderContext: PlaceholderContext<Player>
    ): PacketEntity {
        val packetEntity = Pakket.handler.createEntity(location, EntityType.ITEM_DISPLAY, null)
            ?: throw Exception("Failed to create entity")
        val entityData = createInitialData()
        val packet = Pakket.handler.createEntityUpdatePacket(packetEntity.entityId, entityData)
        packetEntity.updatePacket = packet
        return packetEntity
    }

    override suspend fun tick(hologramLineHandle: HologramLineHandle) {
        val entityData = buildData(hologramLineHandle)
        if (entityData.isEmpty()) return

        val packet = Pakket.handler.createEntityUpdatePacket(hologramLineHandle.packetEntity.entityId, entityData)
        hologramLineHandle.packetEntity.updatePacket = packet
        hologramLineHandle.player.sendPacket(packet, false)
    }

    override fun buildData(placeholderContext: PlaceholderContext<Player>, player: Player): List<EntityDataValue> {
        val data = ArrayList<EntityDataValue>()

        data += cachedData.values.toList()
        cachedData.clear()

        return data
    }

    class Settings(
        val item: ItemStack,
        val height: Double = 0.3,
        val scale: Float = 1.0f,
        val billboard: Billboard = Billboard.CENTER,
        val itemDisplayTransform: ItemDisplayTransform,
        val conditions: List<ConditionHandle<Player>>,
        val failLine: LineSettings?,
        val transformationDuration: Int,
        val teleportInterpolation: Int,
        val translation: Vector3f
    ) : LineSettings {
        override fun create(): HologramLine {
            return ItemHologramLine(
                item,
                height,
                scale,
                billboard,
                itemDisplayTransform,
                { p ->
                    conditions.checkConditions(p)
                },
                failLine?.create(),
                transformationDuration,
                teleportInterpolation,
                translation
            )
        }
    }

}
