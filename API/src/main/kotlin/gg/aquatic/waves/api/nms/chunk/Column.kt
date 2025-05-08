package gg.aquatic.waves.api.nms.chunk

class Column(
    val x: Int,
    val z: Int,
    val fullChunk: Boolean,
    val chunks: Collection<BaseChunk>
) {
}