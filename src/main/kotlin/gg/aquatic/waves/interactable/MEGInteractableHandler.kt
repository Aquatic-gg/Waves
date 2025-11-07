package gg.aquatic.waves.interactable

import com.ticxo.modelengine.api.events.BaseEntityInteractEvent
import com.ticxo.modelengine.api.events.BaseEntityInteractEvent.Action
import gg.aquatic.waves.api.event.event
import org.bukkit.inventory.EquipmentSlot

class MEGInteractableHandler {

    init {
        event<BaseEntityInteractEvent> {
            val base = it.baseEntity
            if (base !is MEGInteractableDummy) return@event
            val interactable = base.interactable
            if (it.slot == EquipmentSlot.OFF_HAND) return@event
            if (it.action == Action.INTERACT_ON) return@event
            it.player.sendMessage("You have interacted!")
            val event = InteractableInteractEvent(
                interactable,
                it.player,
                it.action == Action.ATTACK
            )
            interactable.onInteract(event)
        }

        /*
        event<PlayerChunkLoadEvent> {
            val chunkKey = it.chunk.chunkId()
            for (interactable in megInteractables[chunkKey] ?: return@event) {
                if (interactable.location.world != it.player.world) continue
                if (interactable.audience.canBeApplied(it.player)) {
                    interactable.addViewer(it.player)
                }
            }
        }

        event<PlayerChunkUnloadEvent> {
            val chunkKey = it.chunk.chunkId()
            for (interactable in megInteractables[chunkKey] ?: return@event) {
                if (interactable.location.world != it.player.world) continue
                if (interactable.audience.canBeApplied(it.player)) {
                    interactable.removeViewer(it.player)
                }
            }
        }
         */
    }
}