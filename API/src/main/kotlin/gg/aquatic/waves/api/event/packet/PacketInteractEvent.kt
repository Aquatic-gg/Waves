package gg.aquatic.waves.api.event.packet

import gg.aquatic.waves.api.event.CancellableAquaticEvent
import org.bukkit.entity.Player

class PacketInteractEvent(
    val player: Player,
    val isAttack: Boolean,
    val isSecondary: Boolean,
    val entityId: Int,
    val interactType: InteractType
): CancellableAquaticEvent(true) {

    enum class InteractType {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }

}