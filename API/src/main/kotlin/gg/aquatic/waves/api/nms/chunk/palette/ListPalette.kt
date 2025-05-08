package gg.aquatic.waves.api.nms.chunk.palette

import gg.aquatic.waves.api.nms.readVarInt
import io.netty.buffer.ByteBuf

class ListPalette: Palette {

    override val bits: Int
    val data: IntArray
    var nextId: Int = 0

    constructor(bitsPerEntry: Int) {
        bits = bitsPerEntry
        data = IntArray(1 shl bitsPerEntry)
    }

    constructor(bitsPerEntry: Int, byteBuf: ByteBuf): this(bitsPerEntry) {
        val paletteLength: Int = byteBuf.readVarInt()
        for (i in 0..<paletteLength) {
            this.data[i] = byteBuf.readVarInt()
        }
        this.nextId = paletteLength
    }

    override val size: Int
        get() {
            return data.size
        }

    override fun stateToId(state: Int): Int {
        var id = -1
        for (i in 0..<this.nextId) { // Linear search for state
            if (this.data[i] == state) {
                id = i
                break
            }
        }
        if (id == -1 && this.size < this.data.size) {
            id = this.nextId++
            this.data[id] = state
        }

        return id
    }

    override fun idToState(id: Int): Int {
        return if (id >= 0 && id < this.size) {
            this.data[id]
        } else {
            0
        }
    }
}