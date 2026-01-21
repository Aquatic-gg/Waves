package gg.aquatic.pakket.api.nms

import gg.aquatic.kevent.EventBus
import gg.aquatic.kevent.eventBusBuilder
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.api.nms.profile.GameEventAction
import gg.aquatic.pakket.api.nms.profile.ProfileEntry
import gg.aquatic.pakket.api.nms.scoreboard.Team
import net.kyori.adventure.text.Component
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.util.Vector
import org.joml.Vector3d
import java.util.*

abstract class NMSHandler {

    companion object {
        val eventBus: EventBus by lazy {
            eventBusBuilder {
                this.hierarchical = false
                build()
            }
        }
    }

    abstract fun generateEntityId(): Int

    abstract fun injectPacketListener(player: Player)
    abstract fun unregisterPacketListener(player: Player)
    abstract fun chunkViewers(chunk: Chunk): Collection<Player>
    abstract fun trackedChunks(player: Player): Collection<Chunk>

    abstract fun createBundlePacket(packets: Collection<Any>): Any
    abstract fun createSetSlotItemPacket(inventoryId: Int, stateId: Int, slot: Int, itemStack: ItemStack?): Any
    abstract fun setSlotItem(inventoryId: Int, stateId: Int, slot: Int, itemStack: ItemStack?, vararg players: Player)
    abstract fun createSetWindowItemsPacket(
        inventoryId: Int,
        stateId: Int,
        items: Collection<ItemStack?>,
        carriedItem: ItemStack?
    ): Any

    abstract fun setWindowItems(
        inventoryId: Int,
        stateId: Int,
        items: Collection<ItemStack?>,
        carriedItem: ItemStack?,
        vararg players: Player
    )

    abstract fun showEntity(location: Location, entityType: EntityType, vararg player: Player): PacketEntity?
    abstract fun createEntity(location: Location, entityType: EntityType, uuid: UUID? = null): PacketEntity?
    abstract fun createEntitySpawnPacket(
        entityId: Int,
        uuid: UUID,
        entityType: EntityType,
        pos: Vector3d,
        yaw: Float,
        pitch: Float
    ): Any

    abstract fun recreateEntityPacket(packetEntity: PacketEntity, location: Location): Any
    abstract fun updateEntity(packetEntity: PacketEntity, consumer: (Entity) -> Unit, vararg players: Player)
    abstract fun createEntityUpdatePacket(packetEntity: PacketEntity, consumer: (Entity) -> Unit): Any
    abstract fun createEntityUpdatePacket(id: Int, values: Collection<EntityDataValue>): Any
    abstract fun setPassengers(packetEntity: PacketEntity, passengerIds: IntArray, vararg players: Player)
    abstract fun createPassengersPacket(holderId: Int, passengerIds: IntArray): Any
    abstract fun createDestroyEntitiesPacket(vararg entityIds: Int): Any
    abstract fun createPositionSyncPacket(entityId: Int, location: Location): Any
    abstract fun setEquipment(packetEntity: PacketEntity, equipment: Map<EquipmentSlot, ItemStack?>, vararg players: Player)
    abstract fun createEquipmentPacket(packetEntity: PacketEntity, equipment: Map<EquipmentSlot, ItemStack?>): Any
    abstract fun createTeleportPacket(entityId: Int, location: Location): Any
    abstract fun createPlayerInfoUpdatePacket(actionIds: Collection<Int>, profileEntries: Collection<ProfileEntry>): Any
    abstract fun createPlayerInfoUpdatePacket(actionId: Int, profileEntry: ProfileEntry): Any
    abstract fun createTeamsPacket(team: Team, actionId: Int, playerName: String): Any
    abstract fun createEntityMotionPacket(entityId: Int, motion: Vector): Any

    abstract fun createBlockChangePacket(location: Location, blockState: BlockData): Any
    abstract fun getBukkitEntity(packetEntity: PacketEntity): Entity

    abstract fun createChangeGameStatePacket(action: GameEventAction, value: Float): Any
    abstract fun createCameraPacket(entityId: Int): Any
    //fun modifyChunkPacketBlocks(world: World, packet: Any, func: (List<WrappedChunkSection>) -> Unit)

    abstract fun openWindow(inventoryId: Int, menuType: MenuType, title: Component, vararg players: Player)
    abstract fun closeWindow(inventoryId: Int, vararg players: Player)
    abstract fun closeWindowPacket(inventoryId: Int): Any
    abstract fun openWindowPacket(inventoryId: Int, menuType: MenuType, title: Component): Any
    abstract fun createContainerPropertyPacket(inventoryId: Int, property: Int, value: Int): Any

    abstract fun sendPacket(packet: Any, silent: Boolean = false, vararg players: Player)
    abstract fun sendPacketBundle(bundle: PacketBundle, silent: Boolean = false, vararg players: Player)

    abstract fun receiveWindowClick(
        inventoryId: Int,
        stateId: Int,
        slot: Int,
        buttonNum: Int,
        clickTypeNum: Int,
        carriedItem: ItemStack?,
        changedSlots: Map<Int, ItemStack?>,
        vararg players: Player
    )
}