package gg.aquatic.waves.interactable

import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.util.runLaterSync
import org.bukkit.event.player.PlayerChangedWorldEvent

class BMInteractableHandler {

    init {
        event<PlayerChangedWorldEvent> {
            for (tickableObject in InteractableHandler.bmIntreactables) {
                if (tickableObject.location.world != it.player.world) continue
                tickableObject.removeViewer(it.player)
                runLaterSync(6) {
                    if (tickableObject.audience.canBeApplied(it.player)) {
                        tickableObject.addViewer(it.player)
                    }
                }
            }}
    }

}