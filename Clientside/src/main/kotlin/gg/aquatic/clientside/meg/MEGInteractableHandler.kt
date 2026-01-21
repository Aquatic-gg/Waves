package gg.aquatic.clientside.meg

import com.ticxo.modelengine.api.events.BaseEntityInteractEvent
import gg.aquatic.common.event
import org.bukkit.inventory.EquipmentSlot

class MEGInteractableHandler {

    fun initialize() {
        event<BaseEntityInteractEvent> { event ->
            val dummy = event.baseEntity as? MEGInteractableDummy ?: return@event

            if (event.slot == EquipmentSlot.OFF_HAND) return@event
            if (event.action == BaseEntityInteractEvent.Action.INTERACT_ON) return@event

            val interactable = dummy.interactable
            val isLeft = event.action == BaseEntityInteractEvent.Action.ATTACK

            interactable.handleInteract(event.player, isLeft)
        }
    }
}