package gg.aquatic.waves.api.event.packet

import gg.aquatic.waves.api.event.CancellableAquaticEvent
import gg.aquatic.waves.api.event.PacketEvent
import org.bukkit.entity.Player

class PacketInteractEvent(
    val player: Player,
    val isAttack: Boolean,
    val isSecondary: Boolean,
    val entityId: Int,
    val interactType: InteractType
): PacketEvent() {

    enum class InteractType {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }

}