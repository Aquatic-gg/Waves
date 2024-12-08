package gg.aquatic.waves.inventory

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow.WindowClickType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindowButton
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSlotStateChange
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems
import gg.aquatic.aquaticseries.lib.util.event
import gg.aquatic.waves.Waves
import gg.aquatic.waves.module.WaveModule
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.util.packetEvent
import gg.aquatic.waves.util.player
import gg.aquatic.waves.util.toUser
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

object InventoryManager : WaveModule {

    val openedInventories = ConcurrentHashMap<Player, AquaticInventory>()
    override val type: WaveModules = WaveModules.INVENTORIES

    override fun initialize(waves: Waves) {
        event<PlayerQuitEvent> {
            onCloseMenu(it.player)
        }
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

            val clickData = getClickType(packet)
            if (clickData.second == ClickType.DRAG_START || clickData.second == ClickType.DRAG_ADD) {
                accumulateDrag(player, packet, clickData.second)
                return@packetEvent
            }

            val menuClickData = isMenuClick(packet, Pair(clickData.first, clickData.second), player)
            if (menuClickData) {
                handleClickMenu(WindowClick(player, clickData.second, packet.slot))
                player.updateInventory()
            } else { // isInventoryClick
                handleClickInventory(
                    player,
                    packet
                )
            }
        }
    }

    override fun disable(waves: Waves) {

    }

    fun openMenu(player: Player, inventory: AquaticInventory) {
        openedInventories[player] = inventory
        val viewer = InventoryViewer(player)
        inventory.viewers[player.uniqueId] = viewer

        player.toUser().let {
            it.sendPacket(inventory.inventoryOpenPacket)
            val items = ArrayList<com.github.retrooper.packetevents.protocol.item.ItemStack?>()
            for (i in 0 until inventory.type.size + 36) {
                items += inventory.content[i]?.let {
                    SpigotConversionUtil.fromBukkitItemStack(it)
                }
            }
            val packet = WrapperPlayServerWindowItems(
                126,
                0,
                items,
                viewer.carriedItem
            )
            it.sendPacket(packet)
        }
    }

    fun onCloseMenu(player: Player) {
        val removed = openedInventories.remove(player)
        removed?.viewers?.remove(player.uniqueId)
    }

    fun handleClickInventory(player: Player, packet: WrapperPlayClientClickWindow) {
        val menu = openedInventories[player] ?: error("Menu under player key not found.")
        val clickData = getClickType(packet)

        updateCarriedItem(player, packet.carriedItemStack, clickData.second)

        if (clickData.second == ClickType.DRAG_END) {
            handleDragEnd(player, menu)
        }

        PacketEvents.getAPI().playerManager.receivePacketSilently(this, createAdjustedClickPacket(packet, menu))
    }

    fun handleClickMenu(click: WindowClick) {

        if (click.clickType == ClickType.DRAG_END) {
            clearAccumulatedDrag(click.player)
        }
        val menu = openedInventories[click.player] ?: error("Menu under player key not found.")
        val viewer = menu.viewers[click.player.uniqueId] ?: return
        val carriedItem = viewer.carriedItem ?: com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY
        val items = ArrayList<com.github.retrooper.packetevents.protocol.item.ItemStack?>()
        for (i in 0 until menu.type.size + 36) {
            items += menu.content[i]?.let {
                SpigotConversionUtil.fromBukkitItemStack(it)
            }
        }
        val packet = WrapperPlayServerWindowItems(126, 0, items, carriedItem)
        //val clickedItem = menu.content[click.slot]
        click.player.toUser().sendPacket(packet)
        /*if (clickedItem == null) {
            return
        }
         */
    }


    fun updateItem(inventory: AquaticInventory, item: ItemStack, slot: Int) {
        if (slot > inventory.type.size + 36) return
        inventory.addItem(slot, item)

        val packet = WrapperPlayServerSetSlot(
            126,
            0,
            slot,
            SpigotConversionUtil.fromBukkitItemStack(
                item
            )
        )
        for ((_, viewer) in inventory.viewers) {
            viewer.player.toUser().sendPacket(packet)
        }
    }

    fun updateItems(inventory: AquaticInventory, items: HashMap<Int, ItemStack>) {
        for ((slot, item) in items) {
            inventory.addItem(slot, item)
            //updateItem(inventory, item, slot)
        }

        val items = ArrayList<com.github.retrooper.packetevents.protocol.item.ItemStack?>()
        for (i in 0 until inventory.type.size + 36) {
            items += inventory.content[i]?.let {
                SpigotConversionUtil.fromBukkitItemStack(it)
            }
        }
        for ((_, viewer) in inventory.viewers) {
            val packet = WrapperPlayServerWindowItems(
                126,
                0,
                items,
                viewer.carriedItem
            )
            viewer.player.toUser().sendPacket(packet)
        }
    }

    private fun handleDragEnd(player: Player, inventory: AquaticInventory) {
        val viewer = inventory.viewers[player.uniqueId] ?: return
        viewer.accumulatedDrag.forEach { drag ->
            val packet = if (drag.type == ClickType.DRAG_START) {
                createDragPacket(drag.packet, 0)
            } else {
                createDragPacket(drag.packet, -inventory.type.size + 9)
            }
            PacketEvents.getAPI().playerManager.receivePacketSilently(this, packet)
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
        inventory: AquaticInventory
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
        player.toUser().sendPacket(WrapperPlayServerSetSlot(-1, 0, -1, carriedItem))
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

            else -> null
        }
    }

    fun getClickType(packet: WrapperPlayClientClickWindow): Pair<ButtonType, ClickType> {
        return when (packet.windowClickType) {
            WindowClickType.PICKUP -> {
                if (packet.carriedItemStack != com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY) {
                    if (packet.button == 0) Pair(ButtonType.LEFT, ClickType.PICKUP)
                    else Pair(ButtonType.RIGHT, ClickType.PICKUP)
                } else {
                    Pair(ButtonType.RIGHT, ClickType.PLACE)
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
            in listOf(ClickType.PICKUP, ClickType.PLACE) -> wrapper.slot in slotRange
            ClickType.DRAG_END, ClickType.PICKUP_ALL ->
                wrapper.slot in slotRange || wrapper.slots.orElse(emptyMap()).keys.any { it in slotRange }

            else -> false
        }
    }

}