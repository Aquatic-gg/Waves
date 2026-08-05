package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.entity.Player

class PacketRecipeBookSeenRecipeReceiveEvent(
    val player: Player,
    val recipeId: Int
): PacketEvent()