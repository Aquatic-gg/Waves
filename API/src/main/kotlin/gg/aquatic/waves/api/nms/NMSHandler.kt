package gg.aquatic.waves.api.nms

import gg.aquatic.waves.api.nms.WrappedChunkSection
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.api.nms.profile.GameEventAction
import gg.aquatic.waves.api.nms.profile.ProfileEntry
import net.kyori.adventure.text.Component
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.util.Vector
import java.util.UUID

interface NMSHandler {

    fun generateEntityId(): Int

    fun injectPacketListener(player: Player)
    fun unregisterPacketListener(player: Player)
    fun chunkViewers(chunk: Chunk): Collection<Player>
    fun trackedChunks(player: Player): Collection<Chunk>

    fun createBundlePacket(packets: Collection<Any>): Any
    fun createSetSlotItemPacket(inventoryId: Int, stateId: Int, slot: Int, itemStack: ItemStack?): Any
    fun setSlotItem(inventoryId: Int, stateId: Int, slot: Int, itemStack: ItemStack?, vararg players: Player)
    fun createSetWindowItemsPacket(inventoryId: Int, stateId: Int, items: Collection<ItemStack?>, carriedItem: ItemStack?): Any
    fun setWindowItems(
        inventoryId: Int,
        stateId: Int,
        items: Collection<ItemStack?>,
        carriedItem: ItemStack?,
        vararg players: Player
    )

    fun showEntity(location: Location, entityType: EntityType, vararg player: Player): PacketEntity?
    fun createEntity(location: Location, entityType: EntityType, uuid: UUID? = null): PacketEntity?
    fun recreateEntityPacket(packetEntity: PacketEntity, location: Location): Any
    fun updateEntity(packetEntity: PacketEntity, consumer: (Entity) -> Unit, vararg players: Player)
    fun createEntityUpdatePacket(packetEntity: PacketEntity, consumer: (Entity) -> Unit): Any
    fun createEntityUpdatePacket(id: Int, values: Collection<EntityDataValue>): Any
    fun setPassengers(packetEntity: PacketEntity, passengerIds: IntArray, vararg players: Player)
    fun createPassengersPacket(holderId: Int, passengerIds: IntArray): Any
    fun setEquipment(packetEntity: PacketEntity, equipment: Map<EquipmentSlot, ItemStack?>, vararg players: Player)
    fun createEquipmentPacket(packetEntity: PacketEntity, equipment: Map<EquipmentSlot, ItemStack?>): Any
    fun createTeleportPacket(entityId: Int, location: Location, previousLocation: Vector): Any
    fun createPlayerInfoUpdatePacket(actionIds: Collection<Int>, profileEntries: Collection<ProfileEntry>): Any
    fun createPlayerInfoUpdatePacket(actionId: Int, profileEntry: ProfileEntry): Any
    fun createTeamsPacket(team: gg.aquatic.waves.api.nms.scoreboard.Team, actionId: Int, playerName: String): Any
    fun createEntityMotionPacket(entityId: Int, motion: Vector): Any

    fun createBlockChangePacket(location: Location, blockState: BlockData): Any
    fun getBukkitEntity(packetEntity: PacketEntity): Entity

    fun createChangeGameStatePacket(action: GameEventAction, value: Float): Any
    fun createCameraPacket(entityId: Int): Any
    fun modifyChunkPacketBlocks(world: World, packet: Any, func: (List<WrappedChunkSection>) -> Unit)

    fun openWindow(inventoryId: Int, menuType: MenuType, title: Component, vararg players: Player)
    fun openWindowPacket(inventoryId: Int, menuType: MenuType, title: Component): Any
    fun sendPacket(packet: Any, silent: Boolean = false, vararg players: Player)
    fun sendPacketBundle(budnle: PacketBundle, silent: Boolean = false, vararg players: Player)

    fun receiveWindowClick(
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