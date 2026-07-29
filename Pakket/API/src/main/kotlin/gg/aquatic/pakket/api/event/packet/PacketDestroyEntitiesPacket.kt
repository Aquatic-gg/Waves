package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.entity.Player

data class PacketDestroyEntitiesPacket(
    val player: Player,
    val entityIds: IntArray
): PacketEvent() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PacketDestroyEntitiesPacket

        if (player != other.player) return false
        if (!entityIds.contentEquals(other.entityIds)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = player.hashCode()
        result = 31 * result + entityIds.contentHashCode()
        return result
    }
}