package gg.aquatic.waves.chunk

import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData
import gg.aquatic.waves.Waves
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.util.event.call
import gg.aquatic.waves.util.event.event
import gg.aquatic.waves.util.packetEvent
import gg.aquatic.waves.util.player
import gg.aquatic.waves.util.runAsync
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkUnloadEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ChunkTracker : WavesModule {

    // WorldName, Chunk ID, List of players
    val chunks = ConcurrentHashMap<String, ConcurrentHashMap<ChunkId, MutableSet<UUID>>>()
    val playerToChunks = ConcurrentHashMap<UUID, Pair<String, MutableSet<ChunkId>>>()

    override val type: WaveModules = WaveModules.CHUNK_TRACKER

    override fun initialize(waves: Waves) {
        packetEvent<PacketSendEvent> {
            if (this.packetType != Play.Server.CHUNK_DATA) return@packetEvent
            val packet = WrapperPlayServerChunkData(this)
            val player = this.player() ?: return@packetEvent

            val chunkX = packet.column.x
            val chunkZ = packet.column.z
            val chunkId = ChunkId(chunkX, chunkZ)

            chunks.getOrPut(player.world.name) { ConcurrentHashMap() }.getOrPut(chunkId) { ConcurrentHashMap.newKeySet() }
                .add(player.uniqueId)

            var (previousWorldName, worldChunks) = playerToChunks.getOrPut(player.uniqueId) { player.world.name to ConcurrentHashMap.newKeySet() }
            if (previousWorldName != player.world.name) {
                for (chunkId1 in worldChunks) {
                    val chunkListMap = chunks[previousWorldName] ?: break
                    val chunkList = chunkListMap[chunkId1] ?: continue
                    chunkList -= player.uniqueId
                    AsyncPlayerChunkUnloadEvent(player, chunkId).call()
                    if (chunkList.isEmpty()) {
                        chunks[previousWorldName]?.remove(chunkId1)
                    }
                }
                worldChunks.clear()
                worldChunks = ConcurrentHashMap.newKeySet()
                playerToChunks[player.uniqueId] = player.world.name to worldChunks
            } else {
                val toRemove = mutableSetOf<ChunkId>()
                for (chunkId1 in worldChunks) {
                    val viewDistance = player.clientViewDistance.coerceAtMost(Bukkit.getViewDistance()) + 2
                    val currentChunk = player.location.chunk
                    if (chunkId1.x > currentChunk.x + viewDistance || chunkId1.x < currentChunk.x - viewDistance || chunkId1.z > currentChunk.z + viewDistance || chunkId1.z < currentChunk.z - viewDistance) {
                        toRemove += chunkId1
                        chunks[player.world.name]?.get(chunkId1)?.remove(player.uniqueId)

                        AsyncPlayerChunkUnloadEvent(player, chunkId).call()
                    }
                }
                worldChunks -= toRemove
            }
            worldChunks += chunkId
            AsyncPlayerChunkLoadEvent(player, chunkId, packet).call()

        }
        event<PlayerQuitEvent> {
            val playerPair = playerToChunks.remove(it.player.uniqueId) ?: return@event
            for (chunkId in playerPair.second) {
                val chunkList = chunks[playerPair.first]?.get(chunkId) ?: continue
                chunkList -= it.player.uniqueId
                val chunk = chunkId.toChunk(it.player.world)
                runAsync {
                    AsyncPlayerChunkUnloadEvent(it.player, chunk.chunkId()).call()
                }
                if (chunkList.isNotEmpty()) continue
                chunks[playerPair.first]?.remove(chunkId)

            }
            playerToChunks.remove(it.player.uniqueId)
        }
        event<ChunkUnloadEvent> {
            val chunkId = ChunkId(it.chunk.x, it.chunk.z)
            chunks[it.chunk.world.name]?.remove(chunkId)
            for ((playerUUID, pair) in playerToChunks) {
                val (world, chunkSet) = pair
                if (world != it.chunk.world.name) continue
                if (chunkId !in chunkSet) continue
                chunkSet -= chunkId
                val player = Bukkit.getPlayer(playerUUID) ?: continue
                runAsync {
                    AsyncPlayerChunkUnloadEvent(player, it.chunk.chunkId()).call()
                }
            }
        }
    }

    override fun disable(waves: Waves) {

    }

}