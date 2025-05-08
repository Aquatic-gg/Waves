package gg.aquatic.waves.hologram.line

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.util.Vector3f
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import gg.aquatic.waves.Waves
import gg.aquatic.waves.hologram.*
import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.packetevents.EntityDataBuilder
import gg.aquatic.waves.registry.serializer.RequirementSerializer
import gg.aquatic.waves.util.collection.checkRequirements
import gg.aquatic.waves.util.getSectionList
import gg.aquatic.waves.util.item.loadFromYml
import gg.aquatic.waves.util.requirement.ConfiguredRequirement
import gg.aquatic.waves.util.toUser
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

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
        val packetEntity = Waves.NMS_HANDLER.createEntity(location, EntityType.ITEM_DISPLAY, null)
            ?: throw Exception("Failed to create entity")

        val spawned = SpawnedHologramLine(
            player,
            this,
            packetEntity,
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
        spawnedHologramLine.player.toUser()?.sendPacket(
            WrapperPlayServerEntityTeleport(
                spawnedHologramLine.entityId,
                SpigotConversionUtil.fromBukkitLocation(spawnedHologramLine.currentLocation),
                false
            )
        )
    }

    override fun createEntity(spawnedHologramLine: SpawnedHologramLine) {
        val id = spawnedHologramLine.entityId
        val location = spawnedHologramLine.currentLocation
        val spawnPacket = WrapperPlayServerSpawnEntity(
            id,
            UUID.randomUUID(),
            EntityTypes.ITEM_DISPLAY,
            SpigotConversionUtil.fromBukkitLocation(location),
            location.yaw,
            0,
            null
        )
        val entityData = buildData(spawnedHologramLine)
        val metadataPacket = WrapperPlayServerEntityMetadata(id, entityData)

        val user = spawnedHologramLine.player.toUser() ?: return
        user.sendPacket(spawnPacket)
        user.sendPacket(metadataPacket)
    }

    override fun buildData(spawnedHologramLine: SpawnedHologramLine): List<EntityData> {
        return EntityDataBuilder.ITEM_DISPLAY()
            .setItem(item)
            .setItemTransformation(itemDisplayTransform)
            .setScale(Vector3f(scale, scale, scale))
            .setBillboard(billboard)
            .build()
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