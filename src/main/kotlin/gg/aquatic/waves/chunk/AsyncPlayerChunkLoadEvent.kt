package gg.aquatic.waves.chunk

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData
import gg.aquatic.waves.util.event.AquaticEvent
import org.bukkit.Chunk
import org.bukkit.entity.Player

class AsyncPlayerChunkLoadEvent(
    val player: Player,
    val chunk: ChunkId,
    val wrappedPacket: WrapperPlayServerChunkData
): AquaticEvent(true) {
}