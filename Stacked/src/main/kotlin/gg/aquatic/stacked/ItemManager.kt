package gg.aquatic.stacked

import gg.aquatic.common.event
import gg.aquatic.kregistry.Registry
import gg.aquatic.stacked.ItemHandler.Companion.REGISTRY_KEY
import gg.aquatic.stacked.event.StackedItemInteractEvent
import gg.aquatic.stacked.serialize.ItemSerializer
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot

object ItemManager {

    fun initialize() {
        event<PlayerInteractEvent> {
            handleInteract(it)
        }

        event<PlayerSwapHandItemsEvent> {
            handleSwapHandItems(it)
        }

        event<InventoryClickEvent> {
            handleInventoryClick(it)
        }
    }

    fun injectHandler(id: String, itemHandler: ItemHandler<*, *>) {
        Registry.update {
            itemHandler.id = id
            replaceRegistry(REGISTRY_KEY) { register(id, itemHandler) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : StackedItem<T>> getHandler(id: String): ItemHandler<T, ItemSerializer<T>>? {
        return Registry[REGISTRY_KEY][id] as? ItemHandler<T, ItemSerializer<T>>
    }

    private fun handleInteract(event: PlayerInteractEvent) {
        if (event.hand == EquipmentSlot.OFF_HAND) return
        val item = event.item ?: return
        val aitem = item.toStacked() ?: return
        val handler = aitem.handler
        if (handler.listenInteractions.isEmpty()) return
        val registry = aitem.registryId() ?: return

        val interaction = handler.listenInteractions[registry] ?: return

        val interactType = when (event.action) {
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> if (event.player.isSneaking) StackedItemInteractEvent.InteractType.SHIFT_RIGHT else StackedItemInteractEvent.InteractType.RIGHT
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> if (event.player.isSneaking) StackedItemInteractEvent.InteractType.SHIFT_LEFT else StackedItemInteractEvent.InteractType.LEFT
            else -> return
        }

        val aitemEvent = StackedItemInteractEvent(
            event.player, aitem, item, event, interactType,
        )
        interaction(aitemEvent)
        handler.eventBus.post(aitemEvent)
    }

    private fun handleSwapHandItems(event: PlayerSwapHandItemsEvent) {
        val item = event.mainHandItem
        val aitem = item.toStacked() ?: return
        val registry = aitem.registryId() ?: return

        val handler = aitem.handler
        val interaction = handler.listenInteractions[registry] ?: return

        val interactType =
            if (event.player.isSneaking) StackedItemInteractEvent.InteractType.SHIFT_SWAP else StackedItemInteractEvent.InteractType.SWAP
        val aitemEvent = StackedItemInteractEvent(
            event.player, aitem, item, event, interactType,
        )
        interaction(aitemEvent)
        handler.eventBus.post(aitemEvent)
    }

    private fun handleInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val item = event.currentItem ?: return
        val aitem = item.toStacked() ?: return
        val registry = aitem.registryId() ?: return

        val handler = aitem.handler
        val interaction = handler.listenInteractions[registry] ?: return

        val interactType = when (event.click) {
            ClickType.SHIFT_LEFT -> StackedItemInteractEvent.InteractType.INVENTORY_SHIFT_LEFT
            ClickType.LEFT -> StackedItemInteractEvent.InteractType.INVENTORY_LEFT
            ClickType.SHIFT_RIGHT -> StackedItemInteractEvent.InteractType.INVENTORY_SHIFT_RIGHT
            ClickType.RIGHT -> StackedItemInteractEvent.InteractType.INVENTORY_RIGHT
            ClickType.NUMBER_KEY -> {
                when (event.hotbarButton) {
                    1 -> StackedItemInteractEvent.InteractType.NUM_1
                    2 -> StackedItemInteractEvent.InteractType.NUM_2
                    3 -> StackedItemInteractEvent.InteractType.NUM_3
                    4 -> StackedItemInteractEvent.InteractType.NUM_4
                    5 -> StackedItemInteractEvent.InteractType.NUM_5
                    6 -> StackedItemInteractEvent.InteractType.NUM_6
                    7 -> StackedItemInteractEvent.InteractType.NUM_7
                    8 -> StackedItemInteractEvent.InteractType.NUM_8
                    9 -> StackedItemInteractEvent.InteractType.NUM_9
                    else -> StackedItemInteractEvent.InteractType.NUM_0
                }
            }

            ClickType.DROP -> StackedItemInteractEvent.InteractType.INVENTORY_DROP
            ClickType.SWAP_OFFHAND -> StackedItemInteractEvent.InteractType.INVENTORY_SWAP
            else -> return
        }
        val aitemEvent = StackedItemInteractEvent(
            player,
            aitem,
            item,
            event,
            interactType,
        )
        interaction(aitemEvent)
        handler.eventBus.post(aitemEvent)
    }
}