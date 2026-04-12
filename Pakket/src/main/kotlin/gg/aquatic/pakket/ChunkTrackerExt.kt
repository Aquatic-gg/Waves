package gg.aquatic.pakket

import gg.aquatic.common.ChunkId
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.entity.Player

fun Chunk.chunkId(): ChunkId {
    return ChunkId(this.x, this.z)
}
fun ChunkId.toChunk(world: World): Chunk {
    return world.getChunkAt(this.x, this.z)
}

suspend fun Player.trackedChunks(): Collection<Chunk> {
    return Pakket.handler.trackedChunks(this)
}

suspend fun Player.isChunkTracked(chunk: Chunk): Boolean {
    return chunk.trackedBy(this)
}

suspend fun Chunk.trackedBy(): Collection<Player> {
    return Pakket.handler.chunkViewers(this)
}

suspend fun Chunk.trackedBy(player: Player): Boolean {
    return trackedBy().contains(player)
}
