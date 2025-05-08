package gg.aquatic.waves.api.nms.chunk

import gg.aquatic.waves.api.nms.chunk.palette.ListPalette
import io.netty.buffer.ByteBuf


enum class PaletteType(
    val maxBitsPerEntryForList: Int,
    val maxBitsPerEntryForMap: Int,
    val forceMaxListPaletteSize: Boolean,
    val bitShift: Int
) {
    BIOME(3, 3, false, 2),
    CHUNK(4, 8, true, 4);

    val storageSize = 1 shl bitShift * 3

    fun create(): DataPalette {
        val bitsPerEntry: Int = this.maxBitsPerEntryForList
        val palette = ListPalette(bitsPerEntry)
        val storage = BitStorage(bitsPerEntry, this.storageSize)
        return DataPalette(this,palette, storage)
    }

    fun read(wrapper: ByteBuf): DataPalette {
        val allowSingletonPalette = true
        val lengthPrefix: Boolean = true // False when 1.21.5+
        return DataPalette.read(NetStreamInputWrapper(wrapper), this, allowSingletonPalette, lengthPrefix)
    }
}