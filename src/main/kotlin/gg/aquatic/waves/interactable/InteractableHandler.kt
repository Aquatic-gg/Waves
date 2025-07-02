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

object InteractableHandler: WavesModule {

    /*
    val blockInteractables = mutableListOf<BlockInteractable>()
    val entityInteractables = mutableListOf<EntityInteractable>()
     */
    val megInteractables = mutableListOf<MEGInteractable>()
    val bmIntreactables = mutableListOf<BMInteractable>()
    override val type: WaveModules = WaveModules.INTERACTABLES

    override fun initialize(waves: Waves) {
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") != null) {
            MEGInteractableHandler()
        }
        if (Bukkit.getPluginManager().getPlugin("BetterModel") != null) {
            BMInteractableHandler()
        }
        event<PlayerJoinEvent> {
            for (tickableObject in megInteractables + bmIntreactables) {
                if (tickableObject.location.world != it.player.world) continue
                if (tickableObject.audience.canBeApplied(it.player)) {
                    tickableObject.addViewer(it.player)
                }
            }
        }
        event<PlayerQuitEvent> {
            for (tickableObject in megInteractables + bmIntreactables) {
                tickableObject.removeViewer(it.player)
            }
        }
    }

    override fun disable(waves: Waves) {

    }

}