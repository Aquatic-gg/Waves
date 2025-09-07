package gg.aquatic.waves.item

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.event.call
import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.item.option.ItemOptionHandle
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.registry.isAquaticItem
import gg.aquatic.waves.registry.registryId
import gg.aquatic.waves.util.runSync
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

object ItemHandler : WavesModule {

    val NAMESPACE_KEY by lazy {
        NamespacedKey(Waves.INSTANCE, "Custom_Item_Registry")
    }
    val listenInteractions = mutableMapOf<String, (AquaticItemInteractEvent) -> Unit>()
    override val type: WaveModules = WaveModules.ITEMS

    override fun initialize(waves: Waves) {
        runSync {
            event<PlayerInteractEvent> {
                if (it.hand == EquipmentSlot.OFF_HAND) return@event
                if (listenInteractions.isEmpty()) return@event
                val item = it.item ?: return@event
                val aitem = item.isAquaticItem() ?: return@event
                val registry = aitem.registryId() ?: return@event

                val interaction = listenInteractions[registry] ?: return@event

                val interactType = when (it.action) {
                    Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> if (it.player.isSneaking) AquaticItemInteractEvent.InteractType.SHIFT_RIGHT else AquaticItemInteractEvent.InteractType.RIGHT
                    Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> if (it.player.isSneaking) AquaticItemInteractEvent.InteractType.SHIFT_LEFT else AquaticItemInteractEvent.InteractType.LEFT
                    else -> return@event
                }

                val aitemEvent = AquaticItemInteractEvent(
                    it.player, aitem, item, it, interactType,
                )
                interaction(aitemEvent)
                aitemEvent.call()
            }
            event<PlayerSwapHandItemsEvent> {
                val item = it.mainHandItem ?: return@event
                val aitem = item.isAquaticItem() ?: return@event
                val registry = aitem.registryId() ?: return@event
                val interaction = listenInteractions[registry] ?: return@event

                val interactType =
                    if (it.player.isSneaking) AquaticItemInteractEvent.InteractType.SHIFT_SWAP else AquaticItemInteractEvent.InteractType.SWAP
                val aitemEvent = AquaticItemInteractEvent(
                    it.player, aitem, item, it, interactType,
                )
                interaction(aitemEvent)
                aitemEvent.call()
            }
            event<InventoryClickEvent> {
                val player = it.whoClicked as? Player ?: return@event
                val item = it.currentItem ?: return@event
                val aitem = item.isAquaticItem() ?: return@event
                val registry = aitem.registryId() ?: return@event
                val interaction = listenInteractions[registry] ?: return@event

                val interactType = when (it.click) {
                    ClickType.SHIFT_LEFT -> AquaticItemInteractEvent.InteractType.INVENTORY_SHIFT_LEFT
                    ClickType.LEFT -> AquaticItemInteractEvent.InteractType.INVENTORY_LEFT
                    ClickType.SHIFT_RIGHT -> AquaticItemInteractEvent.InteractType.INVENTORY_SHIFT_RIGHT
                    ClickType.RIGHT -> AquaticItemInteractEvent.InteractType.INVENTORY_RIGHT
                    ClickType.NUMBER_KEY -> {
                        when (it.hotbarButton) {
                            1 -> AquaticItemInteractEvent.InteractType.NUM_1
                            2 -> AquaticItemInteractEvent.InteractType.NUM_2
                            3 -> AquaticItemInteractEvent.InteractType.NUM_3
                            4 -> AquaticItemInteractEvent.InteractType.NUM_4
                            5 -> AquaticItemInteractEvent.InteractType.NUM_5
                            6 -> AquaticItemInteractEvent.InteractType.NUM_6
                            7 -> AquaticItemInteractEvent.InteractType.NUM_7
                            8 -> AquaticItemInteractEvent.InteractType.NUM_8
                            9 -> AquaticItemInteractEvent.InteractType.NUM_9
                            else -> AquaticItemInteractEvent.InteractType.NUM_0
                        }
                    }

                    ClickType.DROP -> AquaticItemInteractEvent.InteractType.INVENTORY_DROP
                    ClickType.SWAP_OFFHAND -> AquaticItemInteractEvent.InteractType.INVENTORY_SWAP
                    else -> return@event
                }
                val aitemEvent = AquaticItemInteractEvent(
                    player,
                    aitem,
                    item,
                    it,
                    interactType,
                )
                interaction(aitemEvent)
                aitemEvent.call()
            }
        }
    }

    override fun disable(waves: Waves) {

    }

    fun create(
        internalId: String?,
        item: ItemStack,
        options: List<ItemOptionHandle>
    ): AquaticItem {
        return AquaticItem(
            internalId,
            item,
            options
        )
    }

    interface Factory {

        fun create(id: String): ItemStack?

    }

}