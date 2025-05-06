package gg.aquatic.waves.chunk

import gg.aquatic.waves.Waves
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.entity.Player

/*
fun Chunk.trackedBy(): Set<UUID> {
    val map = ChunkTracker.chunks[this.world.name] ?: return setOf()
    return map[this.chunkId()] ?: setOf()
}

 */
/*
fun Chunk.trackedByPlayers(): Set<Player> {
    val map = ChunkTracker.chunks[this.world.name] ?: return setOf()
    return map[this.chunkId()]?.mapNotNull { Bukkit.getPlayer(it) }?.toSet() ?: setOf()
}

 */

fun Chunk.chunkId(): ChunkId {
    return ChunkId(this.x, this.z)
}
fun ChunkId.toChunk(world: World): Chunk {
    return world.getChunkAt(this.x, this.z)
}
/*
fun Player.trackedChunks(): Set<ChunkId> {
    return ChunkTracker.playerToChunks[this.uniqueId]?.second ?: setOf()
}

 */

fun Player.trackedChunks(): Collection<Chunk> {
    return Waves.NMS_HANDLER.trackedChunks(this)
}

fun Player.isChunkTracked(chunk: Chunk): Boolean {
    return chunk.trackedBy(this)
}

fun Chunk.trackedBy(): Collection<Player> {
    return Waves.NMS_HANDLER.chunkViewers(this)
}

fun Chunk.trackedBy(player: Player): Boolean {
    return trackedBy().contains(player)
}