package gg.aquatic.waves.interactable

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.interactable.type.BMInteractable
import gg.aquatic.waves.interactable.type.MEGInteractable
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.module.WavesModule
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.ConcurrentHashMap

object InteractableHandler: WavesModule {

    /*
    val blockInteractables = mutableListOf<BlockInteractable>()
    val entityInteractables = mutableListOf<EntityInteractable>()
     */
    val megInteractables = ConcurrentHashMap.newKeySet<MEGInteractable>()
    val bmIntreactables = mutableListOf<BMInteractable>()
    override val type: WaveModules = WaveModules.INTERACTABLES

    override fun initialize(waves: Waves) {
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") != null) {
            MEGInteractableHandler()
        }
        if (Bukkit.getPluginManager().getPlugin("BetterModel") != null) {
            BMInteractableHandler()
        }
        event<PlayerJoinEvent> { e ->
            for (tickableObject in bmIntreactables) {
                if (tickableObject.location.world != e.player.world) continue
                if (tickableObject.audience.canBeApplied(e.player)) {
                    tickableObject.addViewer(e.player)
                }
            }

            for (tickableObject in megInteractables) {
                if (tickableObject.audience.canBeApplied(e.player)) {
                    tickableObject.addViewer(e.player)
                }
            }
        }

        event<PlayerQuitEvent> {
            for (tickableObject in megInteractables) {
                tickableObject.removeViewer(it.player)
            }
        }

        /*
        event<PlayerQuitEvent> { e ->
            for (tickableObject in megInteractables.flatMap { it.value } + bmIntreactables) {
                tickableObject.removeViewer(e.player)
            }

         */
    }

    override fun disable(waves: Waves) {

    }

}