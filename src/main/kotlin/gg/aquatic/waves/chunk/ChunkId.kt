package gg.aquatic.waves.chunk

data class ChunkId(
    val x: Int,
    val z: Int
) {
    companion object {
        fun fromChunkKey(key: Long): ChunkId {
            val x = (key and 0xffffffffL).toInt()
            val z = (key shr 32).toInt()
            return ChunkId(x, z)
        }
    }


    val chunkKey: Long = x.toLong() and 0xffffffffL or ((z.toLong() and 0xffffffffL) shl 32)
}