package gg.aquatic.waves.hologram

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.PacketEntity
import org.bukkit.Location
import org.bukkit.entity.Player

class SpawnedHologramLine(
    val hologram: AquaticHologram,
    val player: Player,
    val line: HologramLine,
    location: Location,
    val textUpdater: (Player, String) -> String,
    var packetEntity: PacketEntity,
    var seat: Int? = null
) {

    init {
        packetEntity.seatPacket = getSeatPacket()
        packetEntity.sendSpawnComplete(Waves.NMS_HANDLER, false, player)
    }

    internal fun setAsPassenger(seat: Int?) {
        if (seat == null && this.seat == null) {
            return
        }
        if (seat == null) {
            val packet = Waves.NMS_HANDLER.createPassengersPacket(this.seat!!, intArrayOf(packetEntity.entityId))
            packetEntity.seatPacket = packet
        } else {
            val packet = Waves.NMS_HANDLER.createPassengersPacket(seat, intArrayOf(packetEntity.entityId))
            packetEntity.seatPacket = packet
        }
        this.seat = seat
        packetEntity.sendSeatUpdate(Waves.NMS_HANDLER, false, player)
    }

    private fun getSeatPacket(): Any? {
        return seat?.let {
            Waves.NMS_HANDLER.createPassengersPacket(it, intArrayOf(packetEntity.entityId))
        }
    }
    var currentLocation: Location = location
        private set

    fun tick() {
        line.tick(this)
    }

    fun move(location: Location) {
        currentLocation = location
        packetEntity.teleport(Waves.NMS_HANDLER, location, false, player)
    }

    fun destroy() {
        packetEntity.sendDespawn(Waves.NMS_HANDLER, false, player)
    }
}