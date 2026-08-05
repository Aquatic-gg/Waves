package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerRecipeBookSettingsChangeEvent

class PacketRecipeBookChangeSettingsReceiveEvent(
    val player: Player,
    val type: PlayerRecipeBookSettingsChangeEvent.RecipeBookType,
    val isOpen: Boolean,
    val filtering: Boolean
): PacketEvent()