package gg.aquatic.waves.api.nms.chunk.palette

import gg.aquatic.waves.api.nms.readVarInt
import io.netty.buffer.ByteBuf


class MapPalette: Palette {

    override val bits: Int
    val idToState: IntArray
    val stateToId = HashMap<Any, Int>()
    private var nextId = 0

    constructor(bitsPerEntry: Int) {
        bits = bitsPerEntry
        idToState = IntArray(1 shl bitsPerEntry)
    }

    constructor(bitsPerEntry: Int, byteBuf: ByteBuf): this(bitsPerEntry) {
        val paletteLength: Int = byteBuf.readVarInt()
        for (i in 0..<paletteLength) {
            val state = byteBuf.readVarInt()
            idToState[i] = state
            stateToId[state] = i
        }
        nextId = paletteLength
    }

    override val size: Int
        get() {
            return nextId
        }
    override fun stateToId(state: Int): Int {
        var id = this.stateToId.get(state)
        if (id == null && this.size < this.idToState.size) {
            id = this.nextId++
            this.idToState[id] = state
            this.stateToId.put(state, id)
        }

        return id ?: -1
    }
    override fun idToState(id: Int): Int {
        return if (id >= 0 && id < this.size) {
            this.idToState[id]
        } else {
            0
        }
    }

}