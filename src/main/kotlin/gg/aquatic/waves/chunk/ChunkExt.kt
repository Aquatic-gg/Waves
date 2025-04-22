package gg.aquatic.waves.chunk

import org.bukkit.Chunk
import org.bukkit.World

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