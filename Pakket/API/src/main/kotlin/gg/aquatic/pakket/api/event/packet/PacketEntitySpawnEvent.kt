package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*

data class PacketEntitySpawnEvent(
    val player: Player,
    val entityId: Int,
    val uuid: UUID,
    val entityType: EntityType,
    val location: Location,
): PacketEvent()