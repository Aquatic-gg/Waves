package gg.aquatic.waves.chunk

import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData
import gg.aquatic.waves.Waves
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.util.event.call
import gg.aquatic.waves.util.packetEvent
import gg.aquatic.waves.util.player

object ChunkTracker : WavesModule {

    // WorldName, Chunk ID, List of players
    //val chunks = ConcurrentHashMap<String, ConcurrentHashMap<ChunkId, MutableSet<UUID>>>()
    //val playerToChunks = ConcurrentHashMap<UUID, Pair<String, MutableSet<ChunkId>>>()

    override val type: WaveModules = WaveModules.CHUNK_TRACKER

    override fun initialize(waves: Waves) {
        packetEvent<PacketSendEvent> {
            if (this.packetType != Play.Server.CHUNK_DATA) return@packetEvent
            val packet = WrapperPlayServerChunkData(this)
            val player = this.player() ?: return@packetEvent

            val chunkX = packet.column.x
            val chunkZ = packet.column.z
            val chunkId = ChunkId(chunkX, chunkZ)

            /*
            chunks.getOrPut(player.world.name) { ConcurrentHashMap() }.getOrPut(chunkId) { ConcurrentHashMap.newKeySet() }
                .add(player.uniqueId)

             */

            AsyncPlayerChunkLoadEvent(player, chunkId, packet).call()
        }
    }

    override fun disable(waves: Waves) {

    }

}