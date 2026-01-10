package gg.aquatic.kmenu.inventory

import gg.aquatic.execute.coroutine.BukkitCtx
import gg.aquatic.kevent.eventBusBuilder
import gg.aquatic.kmenu.KMenu
import gg.aquatic.kmenu.coroutine.KMenuCtx
import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryCloseEvent
import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.menu.MenuHandler
import gg.aquatic.kmenu.packetInventory
import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.api.event.packet.*
import gg.aquatic.pakket.packetEvent
import gg.aquatic.pakket.sendPacket
import gg.aquatic.stacked.event
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack

object InventoryModule {

    val eventBus = eventBusBuilder {
        scope = KMenuCtx.scope
        hierarchical = false
    }

    fun onLoad() {
        event<InventoryCloseEvent> {
            onCloseMenu(it.player as? Player ?: return@event, true)
        }

        event<PlayerQuitEvent> {
            onCloseMenu(it.player, false)
        }

        packetEvent<PacketContainerCloseEvent> {
            it.then {
                onCloseMenu(it.player, true)
            }
        }

        packetEvent<PacketContainerOpenEvent> {
            if (shouldIgnore(it.containerId, it.player)) {
                onCloseMenu(it.player, true)
                return@packetEvent
            }
            val inventory = it.player.packetInventory() ?: return@packetEvent
            val viewer = inventory.viewers[it.player.uniqueId] ?: return@packetEvent

            it.then {
                KMenuCtx.launch {
                    updateInventoryContent(inventory, viewer)
                }
            }
        }

        packetEvent<PacketContainerSetSlotEvent> {
            val inventory = it.player.packetInventory() ?: return@packetEvent
            inventory.viewers[it.player.uniqueId] ?: return@packetEvent
            it.cancelled = true
        }

        packetEvent<PacketContainerContentEvent> {
            if (it.inventoryId > 0 && shouldIgnore(it.inventoryId, it.player)) {
                return@packetEvent
            }
            val inventory = it.player.packetInventory() ?: return@packetEvent
            val viewer = inventory.viewers[it.player.uniqueId] ?: return@packetEvent
            it.cancelled = true
            updateInventoryContent(inventory, viewer)
        }

        packetEvent<PacketContainerClickEvent> { packetEvent ->
            try {
                if (shouldIgnore(packetEvent.containerId, packetEvent.player)) return@packetEvent
                packetEvent.cancelled = true

                val inventory = packetEvent.player.packetInventory() ?: return@packetEvent
                val viewer = inventory.viewers[packetEvent.player.uniqueId] ?: return@packetEvent

                val clickData = getClickType(packetEvent, viewer)
                val player = packetEvent.player
                if (clickData.second == ClickType.DRAG_START || clickData.second == ClickType.DRAG_ADD) {
                    accumulateDrag(player, packetEvent, clickData.second)
                    return@packetEvent
                }
                if (packetEvent.slotNum == -999) {
                    inventory.updateItems(player)
                    packetEvent.cancelled = true
                    return@packetEvent
                }
                val changedSlots = packetEvent.changedSlots.mapValues {
                    if (it.key < inventory.type.size) return@mapValues ItemStack.empty()
                    val playerSlot = playerSlotFromMenuSlot(it.key, inventory)
                    val invContent = inventory.content[playerSlot]
                    if (invContent != null) return@mapValues invContent

                    if (playerSlot < -1) return@mapValues ItemStack.empty()
                    player.inventory.getItem(playerSlot) ?: ItemStack.empty()
                }

                val menuClickData = isMenuClick(packetEvent, Pair(clickData.first, clickData.second), player)
                if (menuClickData) {
                    val event = AsyncPacketInventoryInteractEvent(
                        viewer,
                        inventory,
                        packetEvent.slotNum,
                        clickData.first,
                        ItemStack.empty(),
                        changedSlots
                    )

                    player.sendMessage(inventory.javaClass.simpleName)
                    player.sendMessage("Menus equal - ${inventory.javaClass.simpleName}")
                    handleClickMenu(WindowClick(viewer, clickData.second, event.slot))
                    player.sendMessage("Clicked custom menu - ${inventory.javaClass.simpleName}")

                    eventBus.post(event)
                } else { // isInventoryClick
                    handleClickInventory(
                        player,
                        packetEvent,
                        clickData.second,
                        changedSlots
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        MenuHandler.initialize()
    }

    fun openMenu(player: Player, inventory: PacketInventory) {
        val previousMenu = player.packetInventory()
        if (previousMenu != null) {
            onCloseMenu(player, false)
        }

        KMenu.packetInventories[player] = inventory

        val viewer = InventoryViewer(player)
        inventory.viewers[player.uniqueId] = viewer

        inventory.sendInventoryOpenPacket(player)
    }

    fun playerSlotFromMenuSlot(slot: Int, inventory: PacketInventory): Int {
        val offsetSlot = slot - inventory.type.size
        return if (offsetSlot < 27) offsetSlot + 9 else offsetSlot - 27
    }

    private fun onCloseMenu(player: Player, updateContent: Boolean) {
        val removed = player.packetInventory() ?: return
        KMenu.packetInventories.remove(player)

        removed.viewers.remove(player.uniqueId)

        if (!updateContent) return

        eventBus.post(AsyncPacketInventoryCloseEvent(player, removed))

        BukkitCtx {
            player.updateInventory()
        }
    }

    private fun handleClickInventory(
        player: Player,
        packet: PacketContainerClickEvent,
        clickType: ClickType,
        changedSlots: Map<Int, ItemStack>,
    ) {
        val menu = player.packetInventory() ?: error("Menu under player key not found.")
        //updateCarriedItem(player, packet.carriedItem.asItem(), clickType)
        if (clickType == ClickType.DRAG_END) {
            handleDragEnd(player, menu)
        }
        createAdjustedClickPacket(packet, menu, player, changedSlots)
    }

    internal fun updateInventoryContent(inventory: PacketInventory, viewer: InventoryViewer) {
        val items = arrayOfNulls<ItemStack?>(inventory.type.size + 36)
        for (i in 0 until inventory.type.size + 36) {
            val contentItem = inventory.content[i]
            if (contentItem == null && i > inventory.type.lastIndex) {
                val playerItemIndex = playerSlotFromMenuSlot(i, inventory)
                val playerItem = viewer.player.inventory.getItem(playerItemIndex)
                items[i] = playerItem
            } else {
                items[i] = contentItem
            }
        }

        // Offhand item
        val offHandItem = inventory.content[inventory.type.size + 36] ?: viewer.player.inventory.itemInOffHand

        val contentPacket = Pakket.handler.createSetWindowItemsPacket(
            126,
            0,
            items.toList(),
            viewer.carriedItem
        )
        val slotPacket = Pakket.handler.createSetSlotItemPacket(0, 0, 45, offHandItem)
        Pakket.handler.sendPacket(contentPacket, true, viewer.player)
        Pakket.handler.sendPacket(slotPacket, true, viewer.player)
    }

    internal fun updateInventorySlot(inventory: PacketInventory, viewer: InventoryViewer, slot: Int) {
        val contentItem = inventory.content[slot]
        val packet = if (contentItem == null) {
            val playerItemIndex = playerSlotFromMenuSlot(slot, inventory)
            val playerItem = viewer.player.inventory.getItem(playerItemIndex)

            Pakket.handler.createSetSlotItemPacket(126, 0, slot, playerItem)
        } else {
            Pakket.handler.createSetSlotItemPacket(126, 0, slot, contentItem)
        }
        viewer.player.sendPacket(packet, true)
    }

    fun handleClickMenu(click: WindowClick) {

        if (click.clickType == ClickType.DRAG_END) {
            clearAccumulatedDrag(click.player)
        }
        val inventory = click.player.player.packetInventory() ?: error("Menu under player key not found.")
        val viewer = inventory.viewers[click.player.player.uniqueId] ?: return

        updateInventoryContent(inventory, viewer)
    }

    private fun handleDragEnd(player: Player, inventory: PacketInventory) {
        val viewer = inventory.viewers[player.uniqueId] ?: return
        viewer.accumulatedDrag.forEach { drag ->
            if (drag.type == ClickType.DRAG_START) {
                createDragPacket(drag.packet, 0, player)
            } else {
                createDragPacket(drag.packet, -inventory.type.size + 9, player)
            }
        }
        clearAccumulatedDrag(viewer)
    }

    private fun createDragPacket(
        originalPacket: PacketContainerClickEvent,
        slotOffset: Int,
        player: Player,
    ) {
        Pakket.handler.receiveWindowClick(
            0,
            originalPacket.stateId,
            originalPacket.slotNum + slotOffset,
            originalPacket.buttonNum,
            originalPacket.clickTypeId,
            originalPacket.carriedItem,
            originalPacket.changedSlots,
            player,
        )
    }

    fun clearAccumulatedDrag(viewer: InventoryViewer) {
        viewer.accumulatedDrag.clear()
    }

    private fun createAdjustedClickPacket(
        packet: PacketContainerClickEvent,
        inventory: PacketInventory,
        player: Player,
        changedSlots: Map<Int, ItemStack>,
    ) {
        val slotOffset = if (packet.slotNum != -999) packet.slotNum - inventory.type.size + 9 else -999
        val adjustedSlots = changedSlots.mapKeys { (slot, _) ->
            slot - inventory.type.size + 9
        }
        Pakket.handler.receiveWindowClick(
            0,
            packet.stateId,
            slotOffset,
            packet.buttonNum,
            packet.clickTypeId,
            packet.carriedItem,
            adjustedSlots,
            player
        )
    }

    fun accumulateDrag(player: Player, packet: PacketContainerClickEvent, type: ClickType) {
        val inventory = player.packetInventory() ?: return
        val viewer = inventory.viewers[player.uniqueId] ?: return
        viewer.accumulatedDrag.add(AccumulatedDrag(packet, type))
    }

    fun shouldIgnore(id: Int, player: Player): Boolean = id != 126 || player.packetInventory() == null

    fun reRenderCarriedItem(player: Player) {
        val menu = player.packetInventory() ?: error("Menu under player key not found.")
        val carriedItem = menu.viewers[player.uniqueId]?.carriedItem ?: return

        val packet = Pakket.handler.createSetSlotItemPacket(-1, 0, -1, carriedItem)
        player.sendPacket(packet, false)
    }

    private fun updateCarriedItem(
        player: Player,
        carriedItemStack: ItemStack?,
        clickType: ClickType,
    ) {
        val inv = player.packetInventory() ?: return
        val viewer = inv.viewers[player.uniqueId] ?: return
        viewer.carriedItem = when (clickType) {
            ClickType.PICKUP, ClickType.PICKUP_ALL, ClickType.DRAG_START, ClickType.DRAG_END -> {
                carriedItemStack
            }

            ClickType.PLACE -> {
                if (carriedItemStack == null || carriedItemStack.isEmpty || carriedItemStack.type == Material.AIR) null else carriedItemStack
            }

            else -> {
                null
            }
        }
    }

    fun getClickType(event: PacketContainerClickEvent, viewer: InventoryViewer): Pair<ButtonType, ClickType> {
        val carriedItem = event.carriedItem ?: ItemStack.empty()
        return when (event.clickTypeId) {
            0 -> {
                val cursorItem = viewer.carriedItem
                if (event.carriedItem != null && (!carriedItem.isEmpty && carriedItem.type != Material.AIR)
                ) {
                    if (event.buttonNum == 0) Pair(ButtonType.LEFT, ClickType.PICKUP)
                    else Pair(
                        ButtonType.RIGHT,
                        if (cursorItem != null && cursorItem.type != Material.AIR) ClickType.PLACE else ClickType.PICKUP
                    )
                } else {
                    if (event.buttonNum == 0) Pair(ButtonType.LEFT, ClickType.PLACE)
                    else Pair(
                        ButtonType.RIGHT,
                        if (cursorItem?.type == Material.AIR) ClickType.PICKUP else ClickType.PLACE
                    )
                }
            }

            1 -> {
                if (event.buttonNum == 0) {
                    Pair(ButtonType.SHIFT_LEFT, ClickType.SHIFT_CLICK)
                } else {
                    Pair(ButtonType.SHIFT_RIGHT, ClickType.SHIFT_CLICK)
                }
            }

            2 -> {
                if (event.buttonNum == 40) {
                    Pair(ButtonType.F, ClickType.PICKUP)
                } else {
                    Pair(ButtonType.LEFT, ClickType.PLACE)
                }
            }

            3 -> {
                Pair(ButtonType.MIDDLE, ClickType.PICKUP)
            }

            4 -> {
                if (event.buttonNum == 0) {
                    Pair(ButtonType.DROP, ClickType.PICKUP)
                } else {
                    Pair(ButtonType.CTRL_DROP, ClickType.PICKUP)
                }
            }

            5 -> {
                when (event.buttonNum) {
                    0 -> Pair(ButtonType.LEFT, ClickType.DRAG_START)
                    4 -> Pair(ButtonType.RIGHT, ClickType.DRAG_START)
                    8 -> Pair(ButtonType.MIDDLE, ClickType.DRAG_START)

                    1 -> Pair(ButtonType.LEFT, ClickType.DRAG_ADD)
                    5 -> Pair(ButtonType.RIGHT, ClickType.DRAG_ADD)
                    9 -> Pair(ButtonType.MIDDLE, ClickType.DRAG_ADD)

                    2 -> Pair(ButtonType.LEFT, ClickType.DRAG_END)
                    6 -> Pair(ButtonType.RIGHT, ClickType.DRAG_END)
                    10 -> Pair(ButtonType.MIDDLE, ClickType.DRAG_END)

                    else -> Pair(ButtonType.LEFT, ClickType.UNDEFINED)
                }
            }

            6 -> {
                Pair(ButtonType.DOUBLE_CLICK, ClickType.PICKUP_ALL)
            }

            else -> {
                Pair(ButtonType.LEFT, ClickType.UNDEFINED)
            }
        }
    }

    fun isMenuClick(
        wrapper: PacketContainerClickEvent,
        clickType: Pair<ButtonType, ClickType>,
        player: Player,
    ): Boolean {
        val menu = player.packetInventory() ?: error("Menu under player key not found.")
        val slotRange = 0..menu.type.lastIndex

        return when (clickType.second) {
            ClickType.SHIFT_CLICK -> true
            in listOf(
                ClickType.PICKUP,
                ClickType.PLACE
            ),
                -> wrapper.slotNum in slotRange || wrapper.slotNum in menu.content.keys

            ClickType.DRAG_END, ClickType.PICKUP_ALL ->
                (wrapper.slotNum in slotRange || wrapper.slotNum in menu.content.keys) || wrapper.changedSlots.keys.any { it in slotRange }

            else -> false
        }
    }

}