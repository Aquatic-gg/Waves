package gg.aquatic.waves.input

import gg.aquatic.waves.Waves
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.api.event.event
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent

object InputModule: WavesModule {
    override val type: WaveModules = WaveModules.INPUT

    override fun initialize(waves: Waves) {
        event<PlayerQuitEvent> {
            forceCancel(it.player)
        }
    }

    override fun disable(waves: Waves) {
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            forceCancel(onlinePlayer)
        }
    }

    fun forceCancel(player: Player) {
        for (value in WavesRegistry.INPUT_TYPES.values) {
            value.forceCancel(player)
        }
    }
}