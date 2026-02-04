package gg.aquatic.kmenu.inventory

import gg.aquatic.kevent.suspendingEventBusBuilder
import gg.aquatic.kmenu.KMenu
import gg.aquatic.kmenu.bukkit.registerBukkitEvent
import gg.aquatic.kmenu.bukkit.runOnMainThread
import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryCloseEvent
import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.menu.MenuHandler
import gg.aquatic.kmenu.packetInventory
import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.api.event.packet.*
import gg.aquatic.pakket.packetEvent
import gg.aquatic.pakket.sendPacket
import kotlinx.coroutines.launch
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

internal object InventoryHandler {

    val eventBus by lazy {
        suspendingEventBusBuilder {
            scope = KMenu.scope
            hierarchical = false
        }
    }

    fun initialize(plugin: Plugin) {
        registerBukkitEvents(plugin)
        registerPacketEvents()
        MenuHandler.initialize(plugin)
    }

    private fun registerBukkitEvents(plugin: Plugin) {
        registerBukkitEvent<InventoryCloseEvent>(plugin) {
            onCloseMenu(it.player as? Player ?: return@registerBukkitEvent, true)
        }
        registerBukkitEvent<PlayerQuitEvent>(plugin) {
            onCloseMenu(it.player, false)
        }
    }

    private fun registerPacketEvents() {
        packetEvent<PacketContainerCloseEvent> {
            it.then { onCloseMenu(it.player, true) }
        }

        packetEvent<PacketContainerOpenEvent> {
            if (shouldIgnore(it.containerId, it.player)) {
                onCloseMenu(it.player, true)
                return@packetEvent
            }
            val inventory = it.player.packetInventory() ?: return@packetEvent
            val viewer = inventory.viewers[it.player.uniqueId] ?: return@packetEvent

            it.then { KMenu.scope.launch { updateInventoryContent(inventory, viewer) } }
        }

        packetEvent<PacketContainerSetSlotEvent> {
            val inventory = it.player.packetInventory() ?: return@packetEvent
            inventory.viewers[it.player.uniqueId] ?: return@packetEvent
            it.cancelled = true
        }

        packetEvent<PacketContainerContentEvent> {
            if (it.inventoryId > 0 && shouldIgnore(it.inventoryId, it.player)) return@packetEvent
            val inventory = it.player.packetInventory() ?: return@packetEvent
            val viewer = inventory.viewers[it.player.uniqueId] ?: return@packetEvent
            it.cancelled = true
            updateInventoryContent(inventory, viewer)
        }

        packetEvent<PacketContainerClickEvent> { event ->
            handlePacketClick(event)
        }
    }

    private fun handlePacketClick(event: PacketContainerClickEvent) {
        try {
            if (shouldIgnore(event.containerId, event.player)) return
            event.cancelled = true

            withMenuViewer(event.player) { inventory, viewer ->
                handleClickForViewer(event, inventory, viewer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleClickForViewer(
        event: PacketContainerClickEvent,
        inventory: PacketInventory,
        viewer: InventoryViewer,
    ) {
        val player = event.player
        val (buttonType, clickType) = getClickType(event, viewer)

        // 1. Handle Drags
        if (clickType == ClickType.DRAG_START || clickType == ClickType.DRAG_ADD) {
            accumulateDrag(player, event, clickType)
            return
        }

        // 2. Handle Clicks outside the window
        if (event.slotNum == -999) {
            inventory.updateItems(player)
            return
        }

        // 3. Process the interaction
        val changedSlots = calculateChangedSlots(player, inventory, event)

        if (isMenuClick(event, buttonType to clickType, player)) {
            dispatchMenuInteraction(viewer, inventory, event, buttonType, clickType, changedSlots)
        } else {
            handleClickInventory(player, inventory, event, clickType, changedSlots)
        }
    }

    private fun calculateChangedSlots(
        player: Player,
        inventory: PacketInventory,
        event: PacketContainerClickEvent
    ): Map<Int, ItemStack> {
        return event.changedSlots.mapValues { (slot, _) ->
            if (slot < inventory.type.size) return@mapValues ItemStack.empty()

            val playerSlot = playerSlotFromMenuSlot(slot, inventory)
            if (playerSlot < -1) return@mapValues ItemStack.empty()

            inventory.content[playerSlot] ?: player.inventory.getItem(playerSlot) ?: ItemStack.empty()
        }
    }

    private fun dispatchMenuInteraction(
        viewer: InventoryViewer,
        inventory: PacketInventory,
        event: PacketContainerClickEvent,
        button: ButtonType,
        click: ClickType,
        changedSlots: Map<Int, ItemStack>
    ) {
        val interactEvent = AsyncPacketInventoryInteractEvent(
            viewer,
            inventory,
            event.slotNum,
            button,
            ItemStack.empty(),
            changedSlots
        )

        // Internal logic for resetting drag state/re-rendering
        handleClickMenu(WindowClick(viewer, click, interactEvent.slot))

        // Post to external subscribers
        eventBus.post(interactEvent)
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

        runOnMainThread(KMenu.plugin) {
            player.updateInventory()
        }
    }

    private fun handleClickInventory(
        player: Player,
        inventory: PacketInventory,
        packet: PacketContainerClickEvent,
        clickType: ClickType,
        changedSlots: Map<Int, ItemStack>,
    ) {
        //updateCarriedItem(player, packet.carriedItem.asItem(), clickType)
        if (clickType == ClickType.DRAG_END) {
            handleDragEnd(player, inventory)
        }
        createAdjustedClickPacket(packet, inventory, player, changedSlots)
    }

    internal fun updateInventoryContent(inventory: PacketInventory, viewer: InventoryViewer) {
        val items = arrayOfNulls<ItemStack?>(inventory.type.size + 36)
        for (i in 0 until inventory.type.size + 36) {
            items[i] = inventoryItemAt(inventory, viewer, i, allowPlayerFallback = false)
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
        val item = inventoryItemAt(inventory, viewer, slot, allowPlayerFallback = true)
        val packet = Pakket.handler.createSetSlotItemPacket(126, 0, slot, item)
        viewer.player.sendPacket(packet, true)
    }

    fun handleClickMenu(click: WindowClick) {

        if (click.clickType == ClickType.DRAG_END) {
            clearAccumulatedDrag(click.player)
        }
        withMenuViewer(click.player.player) { inventory, viewer ->
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
        clearAccumulatedDrag(viewer)
    }

    private fun createDragPacket(
        originalPacket: PacketContainerClickEvent,
        slotOffset: Int,
        player: Player,
    ) {
        receiveWindowClick(
            originalPacket,
            originalPacket.slotNum + slotOffset,
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
        receiveWindowClick(packet, slotOffset, adjustedSlots, player)
    }

    fun accumulateDrag(player: Player, packet: PacketContainerClickEvent, type: ClickType) {
        withMenuViewer(player) { _, viewer ->
            viewer.accumulatedDrag.add(AccumulatedDrag(packet, type))
        }
    }

    fun shouldIgnore(id: Int, player: Player): Boolean = id != 126 || player.packetInventory() == null

    fun reRenderCarriedItem(player: Player) {
        withMenuViewer(player) { _, viewer ->
            val carriedItem = viewer.carriedItem ?: return@withMenuViewer
            val packet = Pakket.handler.createSetSlotItemPacket(-1, 0, -1, carriedItem)
            player.sendPacket(packet, false)
        }
    }

    private fun updateCarriedItem(
        player: Player,
        carriedItemStack: ItemStack?,
        clickType: ClickType,
    ) {
        withMenuViewer(player) { _, viewer ->
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
    }

    fun getClickType(event: PacketContainerClickEvent, viewer: InventoryViewer): Pair<ButtonType, ClickType> {
        return when (event.clickTypeId) {
            0 -> handleStandardClick(event, viewer)
            1 -> if (event.buttonNum == 0) ButtonType.SHIFT_LEFT to ClickType.SHIFT_CLICK else ButtonType.SHIFT_RIGHT to ClickType.SHIFT_CLICK
            2 -> if (event.buttonNum == 40) ButtonType.F to ClickType.PICKUP else ButtonType.LEFT to ClickType.PLACE
            3 -> ButtonType.MIDDLE to ClickType.PICKUP
            4 -> if (event.buttonNum == 0) ButtonType.DROP to ClickType.PICKUP else ButtonType.CTRL_DROP to ClickType.PICKUP
            5 -> handleDragClick(event.buttonNum)
            6 -> ButtonType.DOUBLE_CLICK to ClickType.PICKUP_ALL
            else -> ButtonType.LEFT to ClickType.UNDEFINED
        }
    }

    private fun handleStandardClick(event: PacketContainerClickEvent, viewer: InventoryViewer): Pair<ButtonType, ClickType> {
        val carried = event.carriedItem ?: ItemStack.empty()
        val cursor = viewer.carriedItem
        val hasCarried = !carried.isEmpty && carried.type != Material.AIR
        val hasCursor = cursor != null && cursor.type != Material.AIR

        return if (event.buttonNum == 0) {
            ButtonType.LEFT to if (hasCarried) ClickType.PICKUP else ClickType.PLACE
        } else {
            val type = if (hasCarried) {
                if (hasCursor) ClickType.PLACE else ClickType.PICKUP
            } else {
                if (cursor?.type == Material.AIR) ClickType.PICKUP else ClickType.PLACE
            }
            ButtonType.RIGHT to type
        }
    }

    private fun handleDragClick(button: Int): Pair<ButtonType, ClickType> {
        return dragClickMap[button] ?: (ButtonType.LEFT to ClickType.UNDEFINED)
    }

    fun isMenuClick(
        wrapper: PacketContainerClickEvent,
        clickType: Pair<ButtonType, ClickType>,
        player: Player,
    ): Boolean {
        val menu = player.packetInventory() ?: error("Menu under player key not found.")
        val slotRange = 0..menu.type.lastIndex

        fun inMenuSlots(slot: Int): Boolean = slot in slotRange || slot in menu.content.keys

        return when (clickType.second) {
            ClickType.SHIFT_CLICK -> true
            ClickType.PICKUP,
            ClickType.PLACE -> inMenuSlots(wrapper.slotNum)

            ClickType.DRAG_END, ClickType.PICKUP_ALL ->
                inMenuSlots(wrapper.slotNum) || wrapper.changedSlots.keys.any { it in slotRange }

            else -> false
        }
    }

    private fun inventoryItemAt(
        inventory: PacketInventory,
        viewer: InventoryViewer,
        slot: Int,
        allowPlayerFallback: Boolean,
    ): ItemStack? {
        val contentItem = inventory.content[slot]
        if (contentItem != null) return contentItem
        if (!allowPlayerFallback && slot <= inventory.type.lastIndex) return null
        val playerItemIndex = playerSlotFromMenuSlot(slot, inventory)
        return viewer.player.inventory.getItem(playerItemIndex)
    }

    private fun receiveWindowClick(
        packet: PacketContainerClickEvent,
        slot: Int,
        changedSlots: Map<Int, ItemStack?>,
        player: Player,
    ) {
        val normalizedSlots = changedSlots.mapValues { (_, item) -> item ?: ItemStack.empty() }
        Pakket.handler.receiveWindowClick(
            0,
            packet.stateId,
            slot,
            packet.buttonNum,
            packet.clickTypeId,
            packet.carriedItem,
            normalizedSlots,
            player
        )
    }

    private inline fun withMenuViewer(
        player: Player,
        block: (PacketInventory, InventoryViewer) -> Unit,
    ) {
        val inventory = player.packetInventory() ?: return
        val viewer = inventory.viewers[player.uniqueId] ?: return
        block(inventory, viewer)
    }

    private val dragClickMap = mapOf(
        0 to (ButtonType.LEFT to ClickType.DRAG_START),
        1 to (ButtonType.LEFT to ClickType.DRAG_ADD),
        2 to (ButtonType.LEFT to ClickType.DRAG_END),
        4 to (ButtonType.RIGHT to ClickType.DRAG_START),
        5 to (ButtonType.RIGHT to ClickType.DRAG_ADD),
        6 to (ButtonType.RIGHT to ClickType.DRAG_END),
        8 to (ButtonType.MIDDLE to ClickType.DRAG_START),
        9 to (ButtonType.MIDDLE to ClickType.DRAG_ADD),
        10 to (ButtonType.MIDDLE to ClickType.DRAG_END),
    )

}
