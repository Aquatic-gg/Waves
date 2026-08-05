package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.entity.Player

data class PacketInteractEvent(
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