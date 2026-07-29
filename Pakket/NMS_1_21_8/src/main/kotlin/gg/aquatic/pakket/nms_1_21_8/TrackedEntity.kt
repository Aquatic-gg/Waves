package gg.aquatic.pakket.nms_1_21_8

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerPlayer
import java.util.function.Predicate

object TrackedEntity: ServerEntity.Synchronizer {
    override fun sendToTrackingPlayers(p0: Packet<in ClientGamePacketListener>) {
    }

    override fun sendToTrackingPlayersAndSelf(p0: Packet<in ClientGamePacketListener>) {
    }

    override fun sendToTrackingPlayersFiltered(
        p0: Packet<in ClientGamePacketListener>,
        p1: Predicate<ServerPlayer>
    ) {
    }
}