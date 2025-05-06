package gg.aquatic.waves.api.nms

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType

interface NMSHandler {

    fun generateEntityId(): Int

    fun setSlotItem(inventoryId: Int, stateId: Int, slot: Int, itemStack: ItemStack?, vararg players: Player)
    fun setWindowItems(
        inventoryId: Int,
        stateId: Int,
        items: Collection<ItemStack?>,
        carriedItem: ItemStack?,
        vararg players: Player
    )

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