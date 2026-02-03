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
import org.bukkit.event.player.PlayerRecipeBookSettingsChangeEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
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
    open fun setSlotItem(inventoryId: Int, stateId: Int, slot: Int, itemStack: ItemStack?, vararg players: Player) {
        val packet = createSetSlotItemPacket(inventoryId, stateId, slot, itemStack)
        sendPacket(packet, silent = false, *players)
    }
    abstract fun createSetWindowItemsPacket(
        inventoryId: Int,
        stateId: Int,
        items: Collection<ItemStack?>,
        carriedItem: ItemStack?
    ): Any

    open fun setWindowItems(
        inventoryId: Int,
        stateId: Int,
        items: Collection<ItemStack?>,
        carriedItem: ItemStack?,
        vararg players: Player
    ) {
        val packet = createSetWindowItemsPacket(inventoryId, stateId, items, carriedItem)
        sendPacket(packet, silent = false, *players)
    }

    open fun showEntity(location: Location, entityType: EntityType, vararg player: Player): PacketEntity? {
        val packetEntity = createEntity(location, entityType, null) ?: return null
        sendPacket(packetEntity.spawnPacket, silent = false, *player)
        return packetEntity
    }
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
    open fun updateEntity(packetEntity: PacketEntity, consumer: (Entity) -> Unit, vararg players: Player) {
        val packet = createEntityUpdatePacket(packetEntity, consumer)
        packetEntity.updatePacket = packet
        sendPacket(packet, silent = false, *players)
    }
    abstract fun createEntityUpdatePacket(packetEntity: PacketEntity, consumer: (Entity) -> Unit): Any
    abstract fun createEntityUpdatePacket(id: Int, values: Collection<EntityDataValue>): Any
    open fun setPassengers(packetEntity: PacketEntity, passengerIds: IntArray, vararg players: Player) {
        val packet = createPassengersPacket(packetEntity.entityId, passengerIds)
        packetEntity.passengerPacket = packet
        sendPacket(packet, silent = false, *players)
    }
    abstract fun createPassengersPacket(holderId: Int, passengerIds: IntArray): Any
    abstract fun createDestroyEntitiesPacket(vararg entityIds: Int): Any
    abstract fun createPositionSyncPacket(entityId: Int, location: Location): Any
    open fun setEquipment(
        packetEntity: PacketEntity,
        equipment: Map<EquipmentSlot, ItemStack?>,
        vararg players: Player
    ) {
        val packet = createEquipmentPacket(packetEntity, equipment)
        sendPacket(packet, silent = false, *players)
    }
    abstract fun createEquipmentPacket(packetEntity: PacketEntity, equipment: Map<EquipmentSlot, ItemStack?>): Any
    abstract fun createTeleportPacket(entityId: Int, location: Location): Any
    abstract fun createPlayerInfoUpdatePacket(actionIds: Collection<Int>, profileEntries: Collection<ProfileEntry>): Any
    open fun createPlayerInfoUpdatePacket(actionId: Int, profileEntry: ProfileEntry): Any {
        return createPlayerInfoUpdatePacket(listOf(actionId), listOf(profileEntry))
    }
    abstract fun createTeamsPacket(team: Team, actionId: Int, playerName: String): Any
    abstract fun createEntityMotionPacket(entityId: Int, motion: Vector): Any

    abstract fun createBlockChangePacket(location: Location, blockState: BlockData): Any
    abstract fun getBukkitEntity(packetEntity: PacketEntity): Entity

    abstract fun createChangeGameStatePacket(action: GameEventAction, value: Float): Any
    abstract fun createCameraPacket(entityId: Int): Any
    //fun modifyChunkPacketBlocks(world: World, packet: Any, func: (List<WrappedChunkSection>) -> Unit)

    open fun openWindow(inventoryId: Int, menuType: MenuType, title: Component, vararg players: Player) {
        val packet = openWindowPacket(inventoryId, menuType, title)
        sendPacket(packet, silent = false, *players)
    }
    open fun closeWindow(inventoryId: Int, vararg players: Player) {
        val packet = closeWindowPacket(inventoryId)
        sendPacket(packet, silent = false, *players)
    }
    abstract fun closeWindowPacket(inventoryId: Int): Any
    abstract fun openWindowPacket(inventoryId: Int, menuType: MenuType, title: Component): Any
    abstract fun createContainerPropertyPacket(inventoryId: Int, property: Int, value: Int): Any

    abstract fun createRecipeBookAddPacket(
        id: Int,
        recipe: Recipe,
        showNotifications: Boolean,
        highlight: Boolean,
        replace: Boolean
    ): Any

    abstract fun createRecipeBookRemovePacket(ids: Collection<Int>): Any

    abstract fun createRecipeBookSettingsPacket(
        type: PlayerRecipeBookSettingsChangeEvent.RecipeBookType,
        isOpen: Boolean,
        filtering: Boolean,
    ): Any

    abstract fun getPlayerInventoryState(player: Player): Int

    abstract fun sendPacket(packet: Any, silent: Boolean = false, vararg players: Player)
    open fun sendPacketBundle(bundle: PacketBundle, silent: Boolean = false, vararg players: Player) {
        val packet = createBundlePacket(bundle.packets)
        sendPacket(packet, silent, *players)
    }

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
