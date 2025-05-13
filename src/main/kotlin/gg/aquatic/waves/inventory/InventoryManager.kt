package gg.aquatic.waves.inventory

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.event.call
import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.api.event.packet.*
import gg.aquatic.waves.inventory.event.AsyncPacketInventoryCloseEvent
import gg.aquatic.waves.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.util.runAsync
import gg.aquatic.waves.util.runLaterSync
import gg.aquatic.waves.util.sendPacket
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

object InventoryManager : WavesModule {

    val openedInventories = ConcurrentHashMap<Player, PacketInventory>()
    override val type: WaveModules = WaveModules.INVENTORIES

    override fun initialize(waves: Waves) {
        event<PlayerQuitEvent> {
            runAsync {
                onCloseMenu(it.player, false)
            }
        }
        /*
        packetEvent<PacketSendEvent> {
            val player = player() ?: return@packetEvent
            if (packetType != PacketType.Play.Server.WINDOW_ITEMS) {
                return@packetEvent
            }
            val packet = WrapperPlayServerWindowItems(this)
            if (shouldIgnore(packet.windowId, player)) return@packetEvent

            val menu = openedInventories[player] ?: return@packetEvent
            val viewer = menu.viewers[player.uniqueId] ?: return@packetEvent
            isCancelled = true
            updateInventoryContent(menu, viewer)
        }
         */
        event<PacketContainerCloseEvent> {
            onCloseMenu(it.player, true)
        }
        event<PacketContainerOpenEvent> { event ->
            if (shouldIgnore(event.containerId, event.player)) return@event
            val inventory = openedInventories[event.player] ?: return@event
            val viewer = inventory.viewers[event.player.uniqueId] ?: return@event

            event.then = {
                updateInventoryContent(inventory, viewer)
            }
        }
        event<PacketContainerSetSlotEvent> { event ->
            val inventory = openedInventories[event.player] ?: return@event
            val viewer = inventory.viewers[event.player.uniqueId] ?: return@event
            event.isCancelled = true
        }
        event<PacketContainerContentEvent> { event ->
            val inventory = openedInventories[event.player] ?: return@event
            val viewer = inventory.viewers[event.player.uniqueId] ?: return@event
            event.isCancelled = true
            //updateInventoryContent(inventory, viewer)
        }
        event<PacketContainerClickEvent> { event ->
            if (shouldIgnore(event.containerId, event.player)) return@event
            event.isCancelled = true

            val inventory = openedInventories[event.player] ?: return@event
            val viewer = inventory.viewers[event.player.uniqueId] ?: return@event

            val clickData = getClickType(event, viewer)
            val player = event.player
            if (clickData.second == ClickType.DRAG_START || clickData.second == ClickType.DRAG_ADD) {
                accumulateDrag(player, event, clickData.second)
                return@event
            }
            if (event.slotNum == -999) {
                inventory.updateItems(player)
                event.isCancelled = true
                return@event
            }
            val changedSlots = event.changedSlots.mapValues {
                inventory.content[it.key] ?: player.inventory.getItem(playerSlotFromMenuSlot(it.key, inventory))
                ?: ItemStack.empty()
            }

            val menuClickData = isMenuClick(event, Pair(clickData.first, clickData.second), player)
            if (menuClickData) {
                handleClickMenu(WindowClick(player, clickData.second, event.slotNum))
                val event = AsyncPacketInventoryInteractEvent(
                    viewer,
                    inventory,
                    event.slotNum,
                    clickData.first,
                    ItemStack.empty(),
                    changedSlots
                )
                event.call()
            } else { // isInventoryClick
                handleClickInventory(
                    player,
                    event,
                    clickData.second,
                    changedSlots
                )
            }
        }
    }

    override fun disable(waves: Waves) {

    }

    fun openMenu(player: Player, inventory: PacketInventory) {
        val previousMenu = openedInventories[player]
        if (previousMenu != null) {
            onCloseMenu(player, false)
        }

        openedInventories[player] = inventory
        val viewer = InventoryViewer(player)
        inventory.viewers[player.uniqueId] = viewer

        inventory.sendInventoryOpenPacket(player)
        /*
        NOTE:

        This is not needed anymore as the packet is sent in the event listener.
         */
        //updateInventoryContent(inventory, viewer)
    }

    private fun menuSlotFromPlayerSlot(slot: Int, inventory: PacketInventory): Int {
        return if (slot < 9) {
            slot + 27
        } else {
            slot - 9
        } + inventory.type.lastIndex
    }

    private fun playerSlotFromMenuSlot(slot: Int, inventory: PacketInventory): Int {
        val offsetSlot = slot - inventory.type.size
        return if (offsetSlot < 27) offsetSlot + 9 else offsetSlot - 27
    }

    fun onCloseMenu(player: Player, updateContent: Boolean) {
        val removed = openedInventories.remove(player) ?: return
        removed.viewers.remove(player.uniqueId)

        if (updateContent) {
            runLaterSync(2) {
                if (openedInventories.containsKey(player)) return@runLaterSync
                player.updateInventory()
            }
        }

        AsyncPacketInventoryCloseEvent(player, removed).call()
    }

    fun handleClickInventory(player: Player, packet: PacketContainerClickEvent, clickType: ClickType, changedSlots: Map<Int, ItemStack>) {
        val menu = openedInventories[player] ?: error("Menu under player key not found.")
        updateCarriedItem(player, null, clickType)

        if (clickType == ClickType.DRAG_END) {
            handleDragEnd(player, menu)
        }

        createAdjustedClickPacket(packet, menu, player, changedSlots)
    }

    internal fun updateInventoryContent(inventory: PacketInventory, viewer: InventoryViewer) {
        val items = ArrayList<ItemStack?>()
        for (i in 0 until inventory.type.size + 36) {
            val contentItem = inventory.content[i]
            if (contentItem == null && i > inventory.type.lastIndex) {
                val playerItemIndex = playerSlotFromMenuSlot(i, inventory)
                val playerItem = viewer.player.inventory.getItem(playerItemIndex)
                items += playerItem
            } else {
                items += contentItem
            }
        }

        // Offhand item
        val offHandItem = inventory.content[inventory.type.size + 36] ?: viewer.player.inventory.itemInOffHand

        val contentPacket = Waves.NMS_HANDLER.createSetWindowItemsPacket(126, 0, items, viewer.carriedItem)
        val slotPacket = Waves.NMS_HANDLER.createSetSlotItemPacket(0, 0, 45, offHandItem)
        viewer.player.sendPacket(contentPacket, true)
        viewer.player.sendPacket(slotPacket, true)
    }

    internal fun updateInventorySlot(inventory: PacketInventory, viewer: InventoryViewer, slot: Int) {
        val contentItem = inventory.content[slot]
        val packet = if (contentItem == null) {
            val playerItemIndex = playerSlotFromMenuSlot(slot, inventory)
            val playerItem = viewer.player.inventory.getItem(playerItemIndex)
            Waves.NMS_HANDLER.createSetSlotItemPacket(126, 0, slot, playerItem)
        } else {
            Waves.NMS_HANDLER.createSetSlotItemPacket(126, 0, slot, contentItem)
        }
        Waves.NMS_HANDLER.sendPacket(packet, true, viewer.player)
    }

    fun handleClickMenu(click: WindowClick) {

        if (click.clickType == ClickType.DRAG_END) {
            clearAccumulatedDrag(click.player)
        }
        val inventory = openedInventories[click.player] ?: error("Menu under player key not found.")
        val viewer = inventory.viewers[click.player.uniqueId] ?: return

        updateInventoryContent(inventory, viewer)
    }


    fun updateItem(inventory: PacketInventory, item: ItemStack?, slot: Int) {
        if (slot > inventory.type.size + 36) return
        if (item == null) {
            inventory.content.remove(slot)
        } else {
            inventory.addItem(slot, item)
        }
        val packet = Waves.NMS_HANDLER.createSetSlotItemPacket(
            126,
            0,
            slot,
            item
        )
        for (player in inventory.viewerPlayers) {
            player.sendPacket(packet, true)
        }
    }

    fun updateItems(inventory: PacketInventory, iS: Map<Int, ItemStack?>) {
        for ((slot, item) in iS) {
            if (item == null) {
                inventory.content.remove(slot)
            } else {
                inventory.addItem(slot, item)
            }
            //updateItem(inventory, item, slot)
        }

        for ((_, viewer) in inventory.viewers) {
            updateInventoryContent(inventory, viewer)
        }
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
        clearAccumulatedDrag(player)
    }

    private fun createDragPacket(
        originalPacket: PacketContainerClickEvent,
        slotOffset: Int,
        player: Player,
    ) {
        Waves.NMS_HANDLER.receiveWindowClick(
            0,
            originalPacket.stateId,
            originalPacket.slotNum + slotOffset,
            originalPacket.buttonNum,
            originalPacket.clickTypeId,
            originalPacket.carriedItem,
            originalPacket.changedSlots,
            player
        )
    }

    fun clearAccumulatedDrag(player: Player) {
        val inventory = openedInventories[player] ?: return
        val viewer = inventory.viewers[player.uniqueId] ?: return
        viewer.accumulatedDrag.clear()
    }

    private fun createAdjustedClickPacket(
        packet: PacketContainerClickEvent,
        inventory: PacketInventory,
        player: Player,
        changedSlots: Map<Int, ItemStack>
    ) {
        val slotOffset = if (packet.slotNum != -999) packet.slotNum - inventory.type.size + 9 else -999
        val adjustedSlots = changedSlots.mapKeys { (slot, _) ->
            slot - inventory.type.size + 9
        }

        Waves.NMS_HANDLER.receiveWindowClick(
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
        val inventory = openedInventories[player] ?: return
        val viewer = inventory.viewers[player.uniqueId] ?: return
        viewer.accumulatedDrag.add(AccumulatedDrag(packet, type))
    }

    fun shouldIgnore(id: Int, player: Player): Boolean = id != 126 || !openedInventories.containsKey(player)

    fun reRenderCarriedItem(player: Player) {
        val menu = openedInventories[player] ?: error("Menu under player key not found.")
        val carriedItem = menu.viewers[player.uniqueId]?.carriedItem ?: return

        Waves.NMS_HANDLER.setSlotItem(-1, 0, -1, carriedItem, player)
    }

    private fun updateCarriedItem(
        player: Player,
        carriedItemStack: ItemStack?,
        clickType: ClickType,
    ) {
        val inv = openedInventories[player] ?: return
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
        return when (event.clickTypeId) {
            0 -> {
                val cursorItem = viewer.carriedItem
                if (event.carriedItem != null && (!event.carriedItem!!.isEmpty && event.carriedItem?.type != Material.AIR)
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
        val menu = openedInventories[player] ?: error("Menu under player key not found.")
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