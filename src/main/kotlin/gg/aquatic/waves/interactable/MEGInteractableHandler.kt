package gg.aquatic.waves.interactable

import com.ticxo.modelengine.api.events.BaseEntityInteractEvent
import com.ticxo.modelengine.api.events.BaseEntityInteractEvent.Action
import gg.aquatic.waves.interactable.type.MEGInteractable
import gg.aquatic.waves.util.event.event
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.inventory.EquipmentSlot

class MEGInteractableHandler {

    init {
        event<BaseEntityInteractEvent> {
            val base = it.baseEntity
            if (base !is MEGInteractableDummy) return@event
            val interactable = base.interactable
            if (it.slot == EquipmentSlot.OFF_HAND) return@event
            if (it.action == Action.INTERACT_ON) return@event
            val event = InteractableInteractEvent(
                interactable,
                it.player,
                it.action == Action.ATTACK
            )
            interactable.onInteract(event)
        }
        event<PlayerChangedWorldEvent> {
            for (tickableObject in InteractableHandler.megInteractables) {
                tickableObject.removeViewer(it.player)
                if (tickableObject.audience.canBeApplied(it.player)) {
                    tickableObject.addViewer(it.player)
                }
            }}
    }

}