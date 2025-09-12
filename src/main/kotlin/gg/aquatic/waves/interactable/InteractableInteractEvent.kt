package gg.aquatic.waves.interactable

import gg.aquatic.waves.api.event.AquaticEvent
import org.bukkit.entity.Player

class InteractableInteractEvent(
    val interactable: Interactable,
    val player: Player,
    val isLeft: Boolean
): AquaticEvent()