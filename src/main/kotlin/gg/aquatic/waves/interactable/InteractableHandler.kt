package gg.aquatic.waves.interactable

import gg.aquatic.waves.Waves
import gg.aquatic.waves.interactable.type.MEGInteractable
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.util.event.event
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object InteractableHandler: WavesModule {

    /*
    val blockInteractables = mutableListOf<BlockInteractable>()
    val entityInteractables = mutableListOf<EntityInteractable>()
     */
    val megInteractables = mutableListOf<MEGInteractable>()
    override val type: WaveModules = WaveModules.INTERACTABLES

    override fun initialize(waves: Waves) {
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") != null) {
            MEGInteractableHandler()
        }
        event<PlayerJoinEvent> {
            for (tickableObject in megInteractables) {
                if (tickableObject.location.world != it.player.world) continue
                if (tickableObject.audience.canBeApplied(it.player)) {
                    tickableObject.addViewer(it.player)
                }
            }
        }
        event<PlayerQuitEvent> {
            for (tickableObject in megInteractables) {
                tickableObject.removeViewer(it.player)
            }
        }
    }

    override fun disable(waves: Waves) {

    }

}