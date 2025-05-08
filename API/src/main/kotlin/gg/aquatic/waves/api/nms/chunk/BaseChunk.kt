package gg.aquatic.waves.api.nms.chunk

import io.netty.buffer.ByteBuf

class BaseChunk {

    companion object {
        const val AIR = 0

        fun read(`in`: ByteBuf): BaseChunk {
            return read(`in`, false) // True when 1.21.5+
        }

        fun read(`in`: ByteBuf, paletteLengthPrefix: Boolean): BaseChunk {
            val blockCount: Int = `in`.readShort().toInt()
            val chunkPalette: DataPalette = DataPalette.read(
                `in`, PaletteType.CHUNK,
                true, paletteLengthPrefix
            )
            val biomePalette: DataPalette = DataPalette.read(
                `in`, PaletteType.BIOME,
                true, paletteLengthPrefix
            )
            return BaseChunk(blockCount, chunkPalette, biomePalette)
        }
    }

    var blockCount = 0

    val chunkData: DataPalette
    val biomeData: DataPalette

    constructor() {
        chunkData = PaletteType.CHUNK.create()
        biomeData = PaletteType.BIOME.create()
    }

    constructor(blockCount: Int, chunkData: DataPalette, biomeData: DataPalette) {
        this.blockCount = blockCount
        this.chunkData = chunkData
        this.biomeData = biomeData
    }

    fun getBlockId(x: Int, y: Int, z: Int): Int {
        return this.chunkData.get(x, y, z)
    }

    fun set(x: Int, y: Int, z: Int, state: Int) {
        val curr = this.chunkData.set(x, y, z, state)
        if (state != AIR && curr == AIR) {
            this.blockCount++
        } else if (state == AIR && curr != AIR) {
            this.blockCount--
        }
    }

    val isEmpty: Boolean
        get() {
            return this.blockCount == 0
        }
}