package gg.aquatic.waves.interactable

import com.ticxo.modelengine.api.events.BaseEntityInteractEvent
import com.ticxo.modelengine.api.events.BaseEntityInteractEvent.Action
import gg.aquatic.waves.api.event.event
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.UUID

class MEGInteractableHandler {

    private val interactionsCache = HashMap<UUID, Long>()

    init {
        event<BaseEntityInteractEvent> {
            val base = it.baseEntity
            if (base !is MEGInteractableDummy) return@event
            val interactable = base.interactable
            if (it.slot == EquipmentSlot.OFF_HAND) return@event
            if (it.action == Action.INTERACT_ON) return@event

            val previousInteraction = interactionsCache[it.player.uniqueId]
            if (previousInteraction != null && previousInteraction + 50 > System.currentTimeMillis()) return@event

            interactionsCache[it.player.uniqueId] = System.currentTimeMillis()

            val event = InteractableInteractEvent(
                interactable,
                it.player,
                it.action == Action.ATTACK
            )
            interactable.onInteract(event)
        }

        event<PlayerQuitEvent> { e ->
            interactionsCache.remove(e.player.uniqueId)
        }
    }
}