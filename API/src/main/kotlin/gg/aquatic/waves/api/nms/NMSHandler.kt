package gg.aquatic.waves.api.nms

import net.kyori.adventure.text.Component
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.util.Vector

interface NMSHandler {

    fun generateEntityId(): Int

    fun injectPacketListener(player: Player)
    fun unregisterPacketListener(player: Player)
    fun chunkViewers(chunk: Chunk): Collection<Player>
    fun trackedChunks(player: Player): Collection<Chunk>

    fun setSlotItem(inventoryId: Int, stateId: Int, slot: Int, itemStack: ItemStack?, vararg players: Player)
    fun setWindowItems(
        inventoryId: Int,
        stateId: Int,
        items: Collection<ItemStack?>,
        carriedItem: ItemStack?,
        vararg players: Player
    )

    fun showEntity(location: Location, entityType: EntityType, vararg player: Player): PacketEntity?
    fun createEntity(location: Location, entityType: EntityType): PacketEntity?
    fun updateEntity(packetEntity: PacketEntity, consumer: (Entity) -> Unit, vararg players: Player)
    fun createEntityUpdatePacket(packetEntity: PacketEntity, consumer: (Entity) -> Unit): Any
    fun setPassengers(packetEntity: PacketEntity, passengerIds: IntArray, vararg players: Player)
    fun createPassengersPacket(packetEntity: PacketEntity, passengerIds: IntArray): Any
    fun setEquipment(packetEntity: PacketEntity, equipment: Map<EquipmentSlot, ItemStack?>, vararg players: Player)
    fun createEquipmentPacket(packetEntity: PacketEntity, equipment: Map<EquipmentSlot, ItemStack?>): Any
    fun createTeleportPacket(entityId: Int, location: Location, previousLocation: Vector): Any

    fun openWindow(inventoryId: Int, menuType: MenuType, title: Component, vararg players: Player)
    fun openWindowPacket(inventoryId: Int, menuType: MenuType, title: Component): Any
    fun sendPacket(packet: Any, silent: Boolean = false, vararg players: Player)

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