package gg.aquatic.clientside.entity

import gg.aquatic.clientside.EntityBased
import gg.aquatic.clientside.FakeObject
import gg.aquatic.clientside.FakeObjectHandler
import gg.aquatic.clientside.ObjectInteractEvent
import gg.aquatic.common.audience.AquaticAudience
import gg.aquatic.common.coroutine.BukkitCtx
import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.api.nms.PacketEntity
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.api.nms.entity.data.impl.ItemEntityData
import gg.aquatic.pakket.sendPacket
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.withContext

class FakeEntity(
    val type: EntityType,
    location: Location,
    viewRange: Int,
    audience: AquaticAudience,
    consumer: FakeEntity.() -> Unit = {},
    var onInteract: ObjectInteractEvent<FakeEntity> = { _, _, _ -> },
    var onUpdate: (Player) -> Unit = {},
    var onTick: suspend () -> Unit = {}
) : FakeObject(viewRange, audience), EntityBased {

    override var location: Location = location
        set(value) {
            field = value
            packetEntity = createPacketEntity()
        }

    private var packetEntity = createPacketEntity()
    override val entityId: Int get() = packetEntity.entityId

    val entityData = ConcurrentHashMap<Int, EntityDataValue>()
    val equipment = ConcurrentHashMap<EquipmentSlot, ItemStack>()
    val passengers: MutableSet<Int> = ConcurrentHashMap.newKeySet()

    init {
        if (type == EntityType.ITEM) {
            setEntityData(ItemEntityData.Item.generate(ItemStack(Material.STONE)))
        }
        updateEntity(consumer)
        setAudience(audience)
    }

    private fun createPacketEntity(): PacketEntity {
        return Pakket.handler.createEntity(location, type) ?: throw Exception("Failed to create NMS entity")
    }

    @Suppress("unused")
    fun setEntityData(vararg dataValues: EntityDataValue) {
        dataValues.forEach { entityData[it.id] = it }
    }

    fun setEntityData(dataValues: Collection<EntityDataValue>) {
        dataValues.forEach { entityData[it.id] = it }
    }

    fun updateEntity(func: FakeEntity.() -> Unit) {
        val hadPassengers = passengers.isNotEmpty()
        func(this)

        packetEntity.updatePacket = Pakket.handler.createEntityUpdatePacket(entityId, entityData.values)
        if (passengers.isNotEmpty()) {
            packetEntity.passengerPacket = Pakket.handler.createPassengersPacket(entityId, passengers.toIntArray())
        }

        packetEntity.equipment.clear()
        packetEntity.equipment += equipment

        val viewers = isViewing.toTypedArray()
        packetEntity.sendDataUpdate(Pakket.handler, false, *viewers)
        if (hadPassengers || passengers.isNotEmpty()) {
            packetEntity.sendPassengerUpdate(Pakket.handler, false, *viewers)
        }
        packetEntity.sendEquipmentUpdate(Pakket.handler, *viewers)
    }

    override fun onShow(player: Player) {
        onUpdate(player)
        packetEntity.sendSpawnComplete(Pakket.handler, false, player)
    }

    override fun onHide(player: Player) {
        packetEntity.sendDespawn(Pakket.handler, false, player)
    }

    override fun handleInteract(player: Player, isLeftClick: Boolean) {
        onInteract.onInteract(this,player, isLeftClick)
    }

    @Suppress("unused")
    suspend fun teleport(newLocation: Location) {
        this.location = newLocation
        if (registered) {
            unregister()
            register()
        }
        val packet = Pakket.handler.createTeleportPacket(entityId, newLocation)
        isViewing.forEach { it.sendPacket(packet, false) }
    }

    override suspend fun register() {
        withContext(BukkitCtx.ofLocation(location)) {
            if (registered) return@withContext
            registered = true
            FakeObjectHandler.tickableObjects += this@FakeEntity
            FakeObjectHandler.idToEntity += entityId to this@FakeEntity

            val chunkX = Math.floorDiv(location.blockX, 16)
            val chunkZ = Math.floorDiv(location.blockZ, 16)

            val bundle = FakeObjectHandler.getOrCreateChunkCacheBundle(chunkX, chunkZ, location.world!!)
            bundle.entities += this@FakeEntity
            bootstrapAudienceViewers()
        }
    }

    fun unregister() {
        if (!registered) return
        registered = false

        val chunkX = Math.floorDiv(location.blockX, 16)
        val chunkZ = Math.floorDiv(location.blockZ, 16)

        val bundle =
            FakeObjectHandler.getChunkCacheBundle(chunkX, chunkZ, location.world!!) ?: return
        bundle.entities -= this
    }

    override fun destroy() {
        if (!markDestroyed()) return
        isViewing.forEach { hide(it) }
        FakeObjectHandler.tickableObjects -= this
        FakeObjectHandler.idToEntity -= entityId
        unregister()
    }

    override suspend fun tick() {
        onTick()
    }

    companion object {
        suspend fun createRegistered(
            type: EntityType,
            location: Location,
            viewRange: Int,
            audience: AquaticAudience,
            consumer: FakeEntity.() -> Unit = {},
            onInteract: ObjectInteractEvent<FakeEntity> = { _, _, _ -> },
            onUpdate: (Player) -> Unit = {},
            onTick: suspend () -> Unit = {}
        ): FakeEntity {
            return withContext(BukkitCtx.ofLocation(location)) {
                FakeEntity(type, location, viewRange, audience, consumer, onInteract, onUpdate, onTick)
                    .also { it.register() }
            }
        }
    }
}
