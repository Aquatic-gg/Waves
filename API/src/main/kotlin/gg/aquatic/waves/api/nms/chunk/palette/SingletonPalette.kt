package gg.aquatic.waves.api.nms.chunk.palette

import gg.aquatic.waves.api.nms.readVarInt
import io.netty.buffer.ByteBuf

class SingletonPalette: Palette {

    private val state: Int

    constructor(state: Int) {
        this.state = state
    }
    constructor(wrapper: ByteBuf): this(wrapper.readVarInt())


    override val size: Int
        get() = 1

    override fun stateToId(state: Int): Int {
        if (this.state == state) {
            return 0
        }
        return -1
    }
    override fun idToState(id: Int): Int {
        if (id == 0) {
            return this.state
        }
        return 0
    }
    override val bits: Int
        get() = 0
}