package gg.aquatic.waves.item

import gg.aquatic.waves.api.event.AquaticEvent
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event

class AquaticItemInteractEvent(
    val player: Player,
    val item: AquaticItem,
    val itemStack: org.bukkit.inventory.ItemStack,
    var originalEvent: Event,
    val interactType: InteractType
): AquaticEvent() {

    var isCancelled: Boolean
        get() {
            return (originalEvent as? Cancellable)?.isCancelled ?: false
        }
        set(value) {
            (originalEvent as? Cancellable)?.isCancelled = value
        }

    enum class InteractType {
        LEFT,
        RIGHT,
        SHIFT_LEFT,
        SHIFT_RIGHT,
        INVENTORY_LEFT,
        INVENTORY_RIGHT,
        INVENTORY_SHIFT_LEFT,
        INVENTORY_SHIFT_RIGHT,
        SWAP,
        SHIFT_SWAP,
        INVENTORY_SWAP,
        DROP,
        INVENTORY_DROP,
        NUM_1,
        NUM_2,
        NUM_3,
        NUM_4,
        NUM_5,
        NUM_6,
        NUM_7,
        NUM_8,
        NUM_9,
        NUM_0,
    }
}