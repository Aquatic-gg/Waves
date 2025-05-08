package gg.aquatic.waves.api.nms.chunk.reader

import gg.aquatic.waves.api.nms.chunk.BaseChunk
import gg.aquatic.waves.api.nms.chunk.BitStorage
import io.netty.buffer.ByteBuf
import java.util.*


object ChunkReader {

    private fun getByteSize(value: Int): Int {
        for (i in 1..4) {
            if ((value and (-1 shl i * 7)) == 0) {
                return i
            }
        }
        return 5
    }

    fun getMojangZeroByteSuffixLength(chunks: Array<BaseChunk?>): Int {
        var mojangPleaseFixThisZeroByteSuffixLength = 0
        for (chunk in chunks) {
            val chunkStorage: BitStorage? = chunk?.chunkData?.storage
            val chunkStorageLen: Int = getByteSize(chunkStorage?.data?.size ?: 0)
            val biomeStorage: BitStorage? = chunk?.biomeData?.storage
            val biomeStorageLen: Int = getByteSize(biomeStorage?.data?.size ?: 0)
            mojangPleaseFixThisZeroByteSuffixLength += chunkStorageLen + biomeStorageLen
        }
        return mojangPleaseFixThisZeroByteSuffixLength
    }

    fun read(
        chunkMask: BitSet?, secondaryChunkMask: BitSet?, fullChunk: Boolean,
        hasBlockLight: Boolean, hasSkyLight: Boolean, chunkSize: Int, arrayLength: Int, wrapper: ByteBuf
    ): Array<BaseChunk?> {
        val ri: Int = wrapper.readerIndex()
        val chunks = arrayOfNulls<BaseChunk>(chunkSize)
        for (i in 0..<chunkSize) {
            chunks[i] = BaseChunk.read(wrapper)
        }
        if (false // true on 1.21.5
            && wrapper.readerIndex() - ri < arrayLength
        ) {
            wrapper.skipBytes(getMojangZeroByteSuffixLength(chunks))
        }
        return chunks
    }

}