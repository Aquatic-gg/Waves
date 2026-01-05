package gg.aquatic.stacked

import gg.aquatic.kevent.eventBusBuilder
import gg.aquatic.stacked.event.StackedItemInteractEvent
import gg.aquatic.stacked.option.ItemOptionHandle
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

object ItemHandler {

    val NAMESPACE_KEY by lazy {
        NamespacedKey(Stacked.plugin, "Custom_Item_Registry")
    }
    val listenInteractions = mutableMapOf<String, (StackedItemInteractEvent) -> Unit>()
    private val configItems = HashSet<String>()

    val eventBus by lazy {
        eventBusBuilder {
            scope = Stacked.scope
            hierarchical = false
        }
    }

    fun initialize() {
        event<PlayerInteractEvent> {
            if (it.hand == EquipmentSlot.OFF_HAND) return@event
            if (listenInteractions.isEmpty()) return@event
            val item = it.item ?: return@event
            val aitem = item.toStacked() ?: return@event
            val registry = aitem.registryId() ?: return@event

            val interaction = listenInteractions[registry] ?: return@event

            val interactType = when (it.action) {
                Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> if (it.player.isSneaking) StackedItemInteractEvent.InteractType.SHIFT_RIGHT else StackedItemInteractEvent.InteractType.RIGHT
                Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> if (it.player.isSneaking) StackedItemInteractEvent.InteractType.SHIFT_LEFT else StackedItemInteractEvent.InteractType.LEFT
                else -> return@event
            }

            val aitemEvent = StackedItemInteractEvent(
                it.player, aitem, item, it, interactType,
            )
            interaction(aitemEvent)
            eventBus.post(aitemEvent)
        }
        event<PlayerSwapHandItemsEvent> {
            val item = it.mainHandItem
            val aitem = item.toStacked() ?: return@event
            val registry = aitem.registryId() ?: return@event
            val interaction = listenInteractions[registry] ?: return@event

            val interactType =
                if (it.player.isSneaking) StackedItemInteractEvent.InteractType.SHIFT_SWAP else StackedItemInteractEvent.InteractType.SWAP
            val aitemEvent = StackedItemInteractEvent(
                it.player, aitem, item, it, interactType,
            )
            interaction(aitemEvent)
            eventBus.post(aitemEvent)
        }
        event<InventoryClickEvent> {
            val player = it.whoClicked as? Player ?: return@event
            val item = it.currentItem ?: return@event
            val aitem = item.toStacked() ?: return@event
            val registry = aitem.registryId() ?: return@event
            val interaction = listenInteractions[registry] ?: return@event

            val interactType = when (it.click) {
                ClickType.SHIFT_LEFT -> StackedItemInteractEvent.InteractType.INVENTORY_SHIFT_LEFT
                ClickType.LEFT -> StackedItemInteractEvent.InteractType.INVENTORY_LEFT
                ClickType.SHIFT_RIGHT -> StackedItemInteractEvent.InteractType.INVENTORY_SHIFT_RIGHT
                ClickType.RIGHT -> StackedItemInteractEvent.InteractType.INVENTORY_RIGHT
                ClickType.NUMBER_KEY -> {
                    when (it.hotbarButton) {
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
                else -> return@event
            }
            val aitemEvent = StackedItemInteractEvent(
                player,
                aitem,
                item,
                it,
                interactType,
            )
            interaction(aitemEvent)
            eventBus.post(aitemEvent)
        }
    }

    fun create(
        factoryId: String?,
        internalId: String?,
        item: ItemStack,
        options: List<ItemOptionHandle>
    ): StackedItem {
        return StackedItem(
            factoryId,
            internalId,
            item,
            options
        )
    }

    interface Factory {

        fun create(id: String): ItemStack?

    }

}