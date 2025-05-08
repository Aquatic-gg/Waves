package gg.aquatic.waves.api.nms.chunk.palette

interface Palette {
    val size: Int
    fun stateToId(state: Int): Int
    fun idToState(id: Int): Int
    val bits: Int
}