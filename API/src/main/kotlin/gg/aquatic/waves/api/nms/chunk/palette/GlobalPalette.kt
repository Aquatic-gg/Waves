package gg.aquatic.waves.api.nms.chunk.palette

class GlobalPalette : Palette {

    companion object {
        const val BITS_PER_ENTRY = 15
        val INSTANCE = GlobalPalette()
    }

    override val size: Int
        get() = Int.MAX_VALUE

    override fun stateToId(state: Int): Int {
        return state
    }

    override fun idToState(id: Int): Int {
        return id
    }

    override val bits: Int
        get() = BITS_PER_ENTRY

}