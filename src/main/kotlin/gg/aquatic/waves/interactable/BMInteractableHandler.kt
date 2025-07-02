package gg.aquatic.waves.interactable

import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.util.runLaterSync
import kr.toxicity.model.api.event.ModelInteractEvent
import kr.toxicity.model.api.nms.HitBox
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import kotlin.jvm.optionals.getOrNull

class BMInteractableHandler {

    init {
        event<PlayerInteractEntityEvent> {
            val hitbox = it.rightClicked as? HitBox ?: return@event
            val registry = hitbox.registry().getOrNull() ?: return@event
            Bukkit.broadcast(Component.text("Interacted 2 with model of UUID ${registry.uuid()}"))
        }
        event<EntityDamageByEntityEvent> {
            val hitbox = it.entity as? HitBox ?: return@event
            val registry = hitbox.registry().getOrNull() ?: return@event
            Bukkit.broadcast(Component.text("Interacted 3 with model of UUID ${registry.uuid()}"))
        }

        event<ModelInteractEvent> {
            val registry = it.hitBox.registry().getOrNull() ?: return@event
            Bukkit.broadcast(Component.text("Interacted with model of UUID ${registry.uuid()}"))
            // TODO
        }

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