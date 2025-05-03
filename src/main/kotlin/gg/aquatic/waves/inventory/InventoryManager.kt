package gg.aquatic.waves.inventory

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow.WindowClickType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems
import gg.aquatic.waves.Waves
import gg.aquatic.waves.inventory.event.AsyncPacketInventoryCloseEvent
import gg.aquatic.waves.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.util.*
import gg.aquatic.waves.api.event.call
import gg.aquatic.waves.api.event.event
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

object InventoryManager : WavesModule {

    val openedInventories = ConcurrentHashMap<Player, PacketInventory>()
    override val type: WaveModules = WaveModules.INVENTORIES

    override fun initialize(waves: Waves) {
        event<PlayerQuitEvent> {
            runAsync {
                onCloseMenu(it.player)
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
        packetEvent<PacketReceiveEvent> {
            val player = player() ?: return@packetEvent

            if (packetType == PacketType.Play.Client.CLOSE_WINDOW) {
                onCloseMenu(player)
                return@packetEvent
            }
            if (packetType != PacketType.Play.Client.CLICK_WINDOW) return@packetEvent
            val packet = WrapperPlayClientClickWindow(this)
            if (shouldIgnore(packet.windowId, player)) return@packetEvent
            isCancelled = true
            val inventory = openedInventories[player] ?: return@packetEvent
            val viewer = inventory.viewers[player.uniqueId] ?: return@packetEvent

            val clickData = getClickType(packet, viewer)
            if (clickData.second == ClickType.DRAG_START || clickData.second == ClickType.DRAG_ADD) {
                accumulateDrag(player, packet, clickData.second)
                return@packetEvent
            }
            if (packet.slot == -999) {
                inventory.updateItems(player)
                isCancelled = true
                return@packetEvent
            }
            val menuClickData = isMenuClick(packet, Pair(clickData.first, clickData.second), player)
            if (menuClickData) {
                handleClickMenu(WindowClick(player, clickData.second, packet.slot))
                val event = AsyncPacketInventoryInteractEvent(
                    viewer,
                    inventory,
                    packet.slot,
                    clickData.first,
                    packet.carriedItemStack?.let { SpigotConversionUtil.toBukkitItemStack(it) },
                    packet.slots.getOrNull()?.mapValues { (_, item) ->
                        SpigotConversionUtil.toBukkitItemStack(item)
                    } ?: emptyMap()
                )
                event.call()
            } else { // isInventoryClick
                handleClickInventory(
                    player,
                    packet,
                    clickData.second
                )
            }
        }
    }

    override fun disable(waves: Waves) {

    }

    fun openMenu(player: Player, inventory: PacketInventory) {
        openedInventories[player] = inventory
        val viewer = InventoryViewer(player)
        inventory.viewers[player.uniqueId] = viewer

        player.toUser()?.let { user ->
            user.sendPacket(inventory.inventoryOpenPacket)
            updateInventoryContent(inventory, viewer)
        }
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

    fun onCloseMenu(player: Player) {
        val removed = openedInventories.remove(player)
        removed?.viewers?.remove(player.uniqueId)

        runLaterSync(2) {
            if (openedInventories.containsKey(player)) return@runLaterSync
            player.updateInventory()
        }
        AsyncPacketInventoryCloseEvent(player, removed ?: return).call()
    }

    fun handleClickInventory(player: Player, packet: WrapperPlayClientClickWindow, clickType: ClickType) {
        val menu = openedInventories[player] ?: error("Menu under player key not found.")
        updateCarriedItem(player, packet.carriedItemStack, clickType)

        if (clickType == ClickType.DRAG_END) {
            handleDragEnd(player, menu)
        }

        PacketEvents.getAPI().playerManager.receivePacketSilently(player, createAdjustedClickPacket(packet, menu))
    }

    internal fun updateInventoryContent(inventory: PacketInventory, viewer: InventoryViewer) {
        val items = ArrayList<com.github.retrooper.packetevents.protocol.item.ItemStack?>()
        for (i in 0 until inventory.type.size + 36) {
            val contentItem = inventory.content[i]
            if (contentItem == null && i > inventory.type.lastIndex) {
                val playerItemIndex = playerSlotFromMenuSlot(i, inventory)
                val playerItem = viewer.player.inventory.getItem(playerItemIndex)
                items += playerItem?.let { SpigotConversionUtil.fromBukkitItemStack(it) }
            } else {
                items += contentItem?.let { SpigotConversionUtil.fromBukkitItemStack(it) }
            }
        }

        // Offhand item
        val offHandItem = inventory.content[inventory.type.size + 36] ?: viewer.player.inventory.itemInOffHand

        val packet = WrapperPlayServerWindowItems(126, 0, items, viewer.carriedItem)
        val offHandPacket = WrapperPlayServerSetSlot(0, 0, 45, SpigotConversionUtil.fromBukkitItemStack(offHandItem))
        viewer.player.toUser()?.let {
            it.sendPacketSilently(packet)
            it.sendPacketSilently(offHandPacket)
        }
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

        val packet = WrapperPlayServerSetSlot(
            126,
            0,
            slot,
            item?.let {
                SpigotConversionUtil.fromBukkitItemStack(
                    it
                )
            } ?: com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY
        )
        for ((_, viewer) in inventory.viewers) {
            viewer.player.toUser()?.sendPacket(packet)
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
            val packet = if (drag.type == ClickType.DRAG_START) {
                createDragPacket(drag.packet, 0)
            } else {
                createDragPacket(drag.packet, -inventory.type.size + 9)
            }
            PacketEvents.getAPI().playerManager.receivePacketSilently(player, packet)
        }
        clearAccumulatedDrag(player)
    }

    private fun createDragPacket(
        originalPacket: WrapperPlayClientClickWindow,
        slotOffset: Int
    ): WrapperPlayClientClickWindow {
        return WrapperPlayClientClickWindow(
            0, originalPacket.stateId, originalPacket.slot + slotOffset, originalPacket.button,
            originalPacket.actionNumber, originalPacket.windowClickType,
            Optional.of(mutableMapOf()), originalPacket.carriedItemStack
        )
    }

    fun clearAccumulatedDrag(player: Player) {
        val inventory = openedInventories[player] ?: return
        val viewer = inventory.viewers[player.uniqueId] ?: return
        viewer.accumulatedDrag.clear()
    }

    private fun createAdjustedClickPacket(
        packet: WrapperPlayClientClickWindow,
        inventory: PacketInventory
    ): WrapperPlayClientClickWindow {
        val slotOffset = if (packet.slot != -999) packet.slot - inventory.type.size + 9 else -999
        val adjustedSlots = packet.slots.orElse(emptyMap()).mapKeys { (slot, _) ->
            slot - inventory.type.size + 9
        }

        return WrapperPlayClientClickWindow(
            0, packet.stateId, slotOffset, packet.button,
            packet.actionNumber, packet.windowClickType,
            Optional.of(adjustedSlots), packet.carriedItemStack
        )
    }

    fun accumulateDrag(player: Player, packet: WrapperPlayClientClickWindow, type: ClickType) {
        val inventory = openedInventories[player] ?: return
        val viewer = inventory.viewers[player.uniqueId] ?: return
        viewer.accumulatedDrag.add(AccumulatedDrag(packet, type))
    }

    fun shouldIgnore(id: Int, player: Player): Boolean = id != 126 || !openedInventories.containsKey(player)

    fun reRenderCarriedItem(player: Player) {
        val menu = openedInventories[player] ?: error("Menu under player key not found.")
        val carriedItem = menu.viewers[player.uniqueId]?.carriedItem ?: return
        player.toUser()?.sendPacket(WrapperPlayServerSetSlot(-1, 0, -1, carriedItem))
    }

    private fun updateCarriedItem(
        player: Player,
        carriedItemStack: com.github.retrooper.packetevents.protocol.item.ItemStack,
        clickType: ClickType
    ) {
        val inv = openedInventories[player] ?: return
        val viewer = inv.viewers[player.uniqueId] ?: return
        viewer.carriedItem = when (clickType) {
            ClickType.PICKUP, ClickType.PICKUP_ALL, ClickType.DRAG_START, ClickType.DRAG_END -> {
                carriedItemStack
            }

            ClickType.PLACE -> {
                if (carriedItemStack == com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY || carriedItemStack.type == ItemTypes.AIR) null else carriedItemStack
            }

            else -> {
                null
            }
        }
    }

    fun getClickType(packet: WrapperPlayClientClickWindow, viewer: InventoryViewer): Pair<ButtonType, ClickType> {
        return when (packet.windowClickType) {
            WindowClickType.PICKUP -> {
                val cursorItem = viewer.carriedItem
                if (packet.carriedItemStack != com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY && packet.carriedItemStack != ItemTypes.AIR
                ) {
                    if (packet.button == 0) Pair(ButtonType.LEFT, ClickType.PICKUP)
                    else Pair(
                        ButtonType.RIGHT,
                        if (cursorItem != null && cursorItem.type != ItemTypes.AIR) ClickType.PLACE else ClickType.PICKUP
                    )
                } else {
                    if (packet.button == 0) Pair(ButtonType.LEFT, ClickType.PLACE)
                    else Pair(
                        ButtonType.RIGHT,
                        if (cursorItem?.type == ItemTypes.AIR) ClickType.PICKUP else ClickType.PLACE
                    )
                }
            }

            WindowClickType.QUICK_MOVE -> {
                if (packet.button == 0) {
                    Pair(ButtonType.SHIFT_LEFT, ClickType.SHIFT_CLICK)
                } else {
                    Pair(ButtonType.SHIFT_RIGHT, ClickType.SHIFT_CLICK)
                }
            }

            WindowClickType.SWAP -> {
                if (packet.button == 40) {
                    Pair(ButtonType.F, ClickType.PICKUP)
                } else {
                    Pair(ButtonType.LEFT, ClickType.PLACE)
                }
            }

            WindowClickType.CLONE -> {
                Pair(ButtonType.MIDDLE, ClickType.PICKUP)
            }

            WindowClickType.THROW -> {
                if (packet.button == 0) {
                    Pair(ButtonType.DROP, ClickType.PICKUP)
                } else {
                    Pair(ButtonType.CTRL_DROP, ClickType.PICKUP)
                }
            }

            WindowClickType.QUICK_CRAFT -> {
                when (packet.button) {
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

            WindowClickType.PICKUP_ALL -> {
                Pair(ButtonType.DOUBLE_CLICK, ClickType.PICKUP_ALL)
            }

            else -> {
                Pair(ButtonType.LEFT, ClickType.UNDEFINED)
            }
        }
    }

    fun isMenuClick(
        wrapper: WrapperPlayClientClickWindow,
        clickType: Pair<ButtonType, ClickType>,
        player: Player
    ): Boolean {
        val menu = openedInventories[player] ?: error("Menu under player key not found.")
        val slotRange = 0..menu.type.lastIndex

        return when (clickType.second) {
            ClickType.SHIFT_CLICK -> true
            in listOf(
                ClickType.PICKUP,
                ClickType.PLACE
            ) -> wrapper.slot in slotRange || wrapper.slot in menu.content.keys

            ClickType.DRAG_END, ClickType.PICKUP_ALL ->
                (wrapper.slot in slotRange || wrapper.slot in menu.content.keys) || wrapper.slots.orElse(emptyMap()).keys.any { it in slotRange }

            else -> false
        }
    }

}