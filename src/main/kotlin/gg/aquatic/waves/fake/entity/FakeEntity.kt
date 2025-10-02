package gg.aquatic.waves.fake.entity

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.PacketEntity
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.chunk.cache.ChunkCacheHandler
import gg.aquatic.waves.chunk.trackedBy
import gg.aquatic.waves.fake.EntityBased
import gg.aquatic.waves.fake.FakeObject
import gg.aquatic.waves.fake.FakeObjectChunkBundle
import gg.aquatic.waves.fake.FakeObjectHandler
import gg.aquatic.waves.fake.entity.data.impl.ItemEntityData
import gg.aquatic.waves.util.*
import gg.aquatic.waves.util.audience.AquaticAudience
import gg.aquatic.waves.util.audience.FilterAudience
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class FakeEntity(
    val type: EntityType, location: Location,
    override val viewRange: Int,
    audience: AquaticAudience,
    consumer: FakeEntity.() -> Unit = {},
    override var onInteract: (FakeEntityInteractEvent) -> Unit = {},
    var onUpdate: (Player) -> Unit = {},
) : FakeObject(), EntityBased {

    @Volatile
    override var audience: AquaticAudience = FilterAudience { false }
        set(value) {
            field = value
            for (viewer in viewers.toMutableList()) {
                if (field.canBeApplied(viewer) && viewer.isOnline) continue
                removeViewer(viewer)
            }
            for (player in
            location.world!!.players.filter { !viewers.contains(it) }) {
                if (!field.canBeApplied(player)) continue
                addViewer(player)
            }
        }

    override var location: Location = location
        set(value) {
            field = value
            packetEntity = createEntity()
        }

    private var packetEntity = createEntity()

    private fun createEntity(): PacketEntity {
        val entity = Waves.NMS_HANDLER.createEntity(location, type) ?: throw Exception("Failed to create entity")
        return entity
    }

    override fun destroy() {
        destroyed = true
        for (player in isViewing.toSet()) {
            hide(player)
        }
        FakeObjectHandler.tickableObjects -= this
        unregister()
        FakeObjectHandler.idToEntity -= entityId
    }

    override val entityId: Int get() = packetEntity.entityId

    val entityData = ConcurrentHashMap<Int, EntityDataValue>()
    val equipment = ConcurrentHashMap<EquipmentSlot, ItemStack>()
    val passengers = ConcurrentHashMap.newKeySet<Int>()

    fun setEntityData(dataValue: EntityDataValue) {
        entityData += dataValue.id to dataValue
    }
    fun setEntityData(vararg dataValue: EntityDataValue) {
        for (data in dataValue) {
            setEntityData(data)
        }
    }
    fun setEntityData(dataValues: Collection<EntityDataValue>) {
        for (data in dataValues) {
            setEntityData(data)
        }
    }

    init {
        if (type == EntityType.ITEM) {
            setEntityData(ItemEntityData.Item.generate(ItemStack(Material.STONE)))
        }
        updateEntity(consumer)
        this.audience = audience
        FakeObjectHandler.tickableObjects += this
        FakeObjectHandler.idToEntity += entityId to this

        val chunkViewers = location.chunk.trackedBy().toSet()
        for (viewer in viewers) {
            if (viewer in chunkViewers) {
                show(viewer)
            }
        }
    }

    fun register() {
        if (registered) return
        registered = true
        var bundle =
            ChunkCacheHandler.getObject(location.chunk, FakeObjectChunkBundle::class.java) as? FakeObjectChunkBundle
        if (bundle == null) {
            bundle = FakeObjectChunkBundle()
            ChunkCacheHandler.registerObject(bundle, location.chunk)
        }
        bundle.entities += this
    }

    fun unregister() {
        if (!registered) return
        registered = false
        val bundle =
            ChunkCacheHandler.getObject(location.chunk, FakeObjectChunkBundle::class.java) as? FakeObjectChunkBundle
                ?: return
        bundle.entities -= this
    }

    fun updateEntity(func: FakeEntity.() -> Unit) {
        val hadPassengers = passengers.isNotEmpty()
        func(this)

        packetEntity.setData(entityData.values)

        if (passengers.isNotEmpty()) {
            packetEntity.setPassengers(passengers.toIntArray())
        }

        packetEntity.setEquipment(equipment)

        val players = isViewing.toTypedArray()
        packetEntity.sendDataUpdate(Waves.NMS_HANDLER, false,*players)
        if (!(!hadPassengers && passengers.isEmpty())) {
            packetEntity.sendPassengerUpdate(Waves.NMS_HANDLER, false,*players)
        }
        packetEntity.sendEquipmentUpdate(Waves.NMS_HANDLER,*players)
    }

    private fun sendUpdate(player: Player) {
        onUpdate(player)
        if (entityData.isNotEmpty()) {
            packetEntity.sendDataUpdate(Waves.NMS_HANDLER, false,player)
        }
        packetEntity.equipment += equipment
        packetEntity.sendPassengerUpdate(Waves.NMS_HANDLER, false,player)
        packetEntity.sendEquipmentUpdate(Waves.NMS_HANDLER,player)
    }

    override fun addViewer(player: Player) {
        if (viewers.contains(player)) return
        viewers.add(player)
        if (player.world.name != location.world!!.name) return
        if (player.location.distanceSquared(location) <= viewRange * viewRange) {
            show(player)
        }
    }

    override fun removeViewer(uuid: UUID) {
        viewers.removeIf { it.uniqueId == uuid }
        isViewing.removeIf { it.uniqueId == uuid }
    }

    override fun removeViewer(player: Player) {
        if (isViewing.contains(player)) {
            hide(player)
        }
        FakeObjectHandler.handlePlayerRemove(player, this, true)
    }

    override fun show(player: Player) {
        if (isViewing.contains(player)) return
        isViewing.add(player)

        onUpdate(player)
        packetEntity.sendSpawnComplete(Waves.NMS_HANDLER,false,player)
    }

    override fun hide(player: Player) {
        isViewing.remove(player)
        packetEntity.sendDespawn(Waves.NMS_HANDLER,false,player)
    }

    override fun tick() {

    }

    fun teleport(location: Location) {
        this.location = location
        if (registered) {
            unregister()
            register()
        }
        val packet = Waves.NMS_HANDLER.createTeleportPacket(packetEntity.entityId,location)
        for (player in isViewing) {
            player.sendPacket(packet,false)
        }
    }
}