package gg.aquatic.waves.api.event.packet

import gg.aquatic.waves.api.event.PacketEvent
import org.bukkit.Location
import org.bukkit.entity.EntityType
import java.util.*

class PacketEntitySpawnEvent(
    val entityId: Int,
    val uuid: UUID,
    val entityType: EntityType,
    val location: Location,
): PacketEvent() {
}