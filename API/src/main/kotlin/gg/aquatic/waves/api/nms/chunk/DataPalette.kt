package gg.aquatic.waves.api.nms.chunk

import gg.aquatic.waves.api.nms.chunk.palette.GlobalPalette
import gg.aquatic.waves.api.nms.chunk.palette.ListPalette
import gg.aquatic.waves.api.nms.chunk.palette.MapPalette
import gg.aquatic.waves.api.nms.chunk.palette.Palette
import gg.aquatic.waves.api.nms.chunk.palette.SingletonPalette
import gg.aquatic.waves.api.nms.readLongs
import gg.aquatic.waves.api.nms.readVarInt
import io.netty.buffer.ByteBuf


class DataPalette(
    val type: PaletteType,
    var palette: Palette,
    var storage: BitStorage?
) {

    fun set(x: Int, y: Int, z: Int, state: Int): Int {
        var id = this.palette.stateToId(state)
        if (id == -1) {
            this.resizeOneUp()
            id = this.palette.stateToId(state)
        }

        if (this.storage != null) {
            val index: Int = index(this.type, x, y, z)
            val curr = this.storage!!.get(index)

            this.storage!!.set(index, id)
            return curr
        } else {
            // Singleton palette and the block has not changed because the palette hasn't resized
            return state
        }
    }

    fun get(x: Int, y: Int, z: Int): Int {
        if (storage != null) {
            val id = this.storage!!.get(index(this.type, x, y, z))
            return this.palette.idToState(id)
        } else {
            return this.palette.idToState(0)
        }
    }

    private fun resizeOneUp() {
        val oldPalette = this.palette
        val oldData = this.storage

        val prevBitsPerEntry = if (oldData != null) oldData.bitsPerEntry else 0
        this.palette = createPalette(prevBitsPerEntry + 1, this.type)
        this.storage = BitStorage(this.palette.bits, this.type.storageSize)

        if (oldData != null) {
            // copy over storage
            var i = 0
            val len: Int = this.type.storageSize
            while (i < len) {
                this.storage!!.set(i, this.palette.stateToId(oldPalette.idToState(oldData.get(i))))
                ++i
            }
        } else {
            this.palette.stateToId(oldPalette.idToState(0))
        }
    }

    companion object {
        private fun createPalette(bitsPerEntry: Int, paletteType: PaletteType): Palette {
            if (bitsPerEntry <= paletteType.maxBitsPerEntryForList) {
                val bits =
                    if (paletteType.forceMaxListPaletteSize) paletteType.maxBitsPerEntryForList else bitsPerEntry
                return ListPalette(bits)
            } else if (bitsPerEntry <= paletteType.maxBitsPerEntryForMap) {
                return MapPalette(bitsPerEntry)
            } else {
                return GlobalPalette.INSTANCE
            }
        }

        private fun index(type: PaletteType,x: Int, y: Int, z: Int): Int {
            return (y shl type.bitShift or z) shl type.bitShift or x
        }

        fun read(wrapper: ByteBuf, paletteType: PaletteType, allowSingletonPalette: Boolean, lengthPrefix: Boolean): DataPalette {
            val bitsPerEntry: Int = wrapper.readByte().toInt()
            val palette: Palette = readPalette(paletteType, bitsPerEntry, wrapper, allowSingletonPalette)
            val storage: BitStorage?
            if (palette !is SingletonPalette) {
                val data: LongArray? = if (lengthPrefix) wrapper.readLongs(wrapper.readVarInt()) else null
                storage = BitStorage(bitsPerEntry, paletteType.storageSize, data)
                if (!lengthPrefix) {
                    // TODO what happens if "bitsPerEntry" != "palette.getBits()"?
                    wrapper.readLongs(storage.data)
                }
            } else {
                if (lengthPrefix) {
                    wrapper.readLongs(wrapper.readVarInt())
                }
                storage = null
            }

            return DataPalette(paletteType,palette, storage)
        }

        private fun readPalette(paletteType: PaletteType, bitsPerEntry: Int, `in`: ByteBuf, allowSingletonPalette: Boolean): Palette {
            if (bitsPerEntry == 0 && allowSingletonPalette) {
                return SingletonPalette(`in`)
            } else if (bitsPerEntry <= paletteType.maxBitsPerEntryForList) {
                // vanilla forces a blockstate-list-palette to always be the maximum size
                val bits =
                    if (paletteType.forceMaxListPaletteSize) paletteType.maxBitsPerEntryForList else bitsPerEntry
                return ListPalette(bits, `in`)
            } else if (bitsPerEntry <= paletteType.maxBitsPerEntryForMap) {
                return MapPalette(bitsPerEntry, `in`)
            } else {
                return GlobalPalette.INSTANCE
            }
        }
    }

}