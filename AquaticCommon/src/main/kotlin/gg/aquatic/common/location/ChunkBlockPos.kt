package gg.aquatic.common.location

import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World

class ChunkBlockPos(
    val x: Int,
    val y: Int,
    val z: Int
) {
    companion object {
        // Minecraft Y offset to make all values positive
        private const val Y_OFFSET = 64

        private const val Y_SPLIT = 128  // Or configure based on your needs
        private const val LOWER_Y_MIN = -64
        private const val LOWER_Y_MAX = 127  // 192 values
        private const val UPPER_Y_MIN = 128
        private const val UPPER_Y_MAX = 319  // 192 values

        fun fromLocation(location: Location): ChunkBlockPos {
            val chunkRelativeX = location.x.toInt() and 0xF  // 0-15
            val chunkRelativeY = location.y.toInt()           // -64 to 319
            val chunkRelativeZ = location.z.toInt() and 0xF  // 0-15

            return ChunkBlockPos(chunkRelativeX, chunkRelativeY, chunkRelativeZ)
        }

        /**
         * Unpacks chunk-relative coordinates from a packed int value
         * Format: [unused(15 bits)][y(9 bits)][z(4 bits)][x(4 bits)]
         * Total: 17 bits used
         */
        fun unpackChunkRelativeLocation(packed: Int): ChunkBlockPos {
            val x = packed and 0xF                      // Bits 0-3: X (4 bits)
            val z = (packed shr 4) and 0xF             // Bits 4-7: Z (4 bits)
            val yOffset = (packed shr 8) and 0x1FF     // Bits 8-16: Y (9 bits)
            val y = yOffset - Y_OFFSET                  // Convert back to actual Y

            return ChunkBlockPos(x, y, z)
        }

        /**
         * 16-bit packing with Y-axis half grouping
         * Format: [y(8 bits, within half)][z(4 bits)][x(4 bits)]
         * Total: 16 bits
         *
         * Y stored as offset within its half (0-191)
         */
        fun unpackChunkRelativeLocation(packed: Short, isUpperHalf: Boolean): ChunkBlockPos {
            val packedInt = packed.toInt() and 0xFFFF
            val x = packedInt and 0xF                      // 4 bits
            val z = (packedInt shr 4) and 0xF             // 4 bits
            val yOffset = (packedInt shr 8) and 0xFF      // 8 bits (0-255)

            // Reconstruct Y based on which half
            val y = if (isUpperHalf) {
                UPPER_Y_MIN + yOffset
            } else {
                LOWER_Y_MIN + yOffset
            }

            return ChunkBlockPos(x, y, z)
        }

        /**
         * Packs multiple ChunkBlockPos into a continuous bit stream
         * 17 bits per position
         */
        fun packMultiple(positions: List<ChunkBlockPos>): ByteArray {
            val totalBits = positions.size * 17
            val totalBytes = (totalBits + 7) / 8  // Round up to nearest byte
            val bytes = ByteArray(totalBytes)

            var bitOffset = 0

            for (pos in positions) {
                val packed = pos.packChunkRelativeLocation()

                // Write 17 bits starting at bitOffset
                writeBits(bytes, bitOffset, packed, 17)
                bitOffset += 17
            }

            return bytes
        }

        /**
         * Unpacks multiple ChunkBlockPos from a continuous bit stream
         */
        fun unpackMultiple(bytes: ByteArray, count: Int): List<ChunkBlockPos> {
            val positions = mutableListOf<ChunkBlockPos>()
            var bitOffset = 0

            repeat(count) {
                val packed = readBits(bytes, bitOffset, 17)
                positions.add(unpackChunkRelativeLocation(packed))
                bitOffset += 17
            }

            return positions
        }

        /**
         * Write `numBits` bits from `value` into `bytes` starting at `bitOffset`
         */
        private fun writeBits(bytes: ByteArray, bitOffset: Int, value: Int, numBits: Int) {
            var remainingBits = numBits
            var currentValue = value
            var currentBitOffset = bitOffset

            while (remainingBits > 0) {
                val byteIndex = currentBitOffset / 8
                val bitIndexInByte = currentBitOffset % 8
                val bitsAvailableInByte = 8 - bitIndexInByte
                val bitsToWrite = minOf(remainingBits, bitsAvailableInByte)

                val mask = ((1 shl bitsToWrite) - 1)
                val bitsToWriteValue = (currentValue and mask)

                // Clear target bits and write new bits
                bytes[byteIndex] = (bytes[byteIndex].toInt() and
                        (((1 shl bitsToWrite) - 1) shl bitIndexInByte).inv()).toByte()
                bytes[byteIndex] = (bytes[byteIndex].toInt() or
                        (bitsToWriteValue shl bitIndexInByte)).toByte()

                currentValue = currentValue shr bitsToWrite
                remainingBits -= bitsToWrite
                currentBitOffset += bitsToWrite
            }
        }

        /**
         * Read `numBits` bits from `bytes` starting at `bitOffset`
         */
        private fun readBits(bytes: ByteArray, bitOffset: Int, numBits: Int): Int {
            var result = 0
            var remainingBits = numBits
            var currentBitOffset = bitOffset
            var resultBitPosition = 0

            while (remainingBits > 0) {
                val byteIndex = currentBitOffset / 8
                val bitIndexInByte = currentBitOffset % 8
                val bitsAvailableInByte = 8 - bitIndexInByte
                val bitsToRead = minOf(remainingBits, bitsAvailableInByte)

                val mask = ((1 shl bitsToRead) - 1)
                val value = (bytes[byteIndex].toInt() shr bitIndexInByte) and mask

                result = result or (value shl resultBitPosition)

                remainingBits -= bitsToRead
                currentBitOffset += bitsToRead
                resultBitPosition += bitsToRead
            }

            return result
        }
    }

    fun isUpperHalf(): Boolean = y >= Y_SPLIT

    fun toWorldLocation(chunk: Chunk): Location {
        return toWorldLocation(chunk.world, chunk.x, chunk.z)
    }

    fun toWorldLocation(world: World, chunkX: Int, chunkZ: Int): Location {
        val worldX = (chunkX shl 4) or x  // chunkX * 16 + x
        val worldZ = (chunkZ shl 4) or z  // chunkZ * 16 + z
        return Location(world, worldX.toDouble(), y.toDouble(), worldZ.toDouble())
    }

    /**
     * Packs chunk-relative coordinates into a single int (only uses 17 bits)
     * Format: [unused(15 bits)][y(9 bits)][z(4 bits)][x(4 bits)]
     *
     * X and Z: 0-15 (4 bits each)
     * Y: -64 to 319 (384 values = 9 bits, stored as 0-383)
     */
    fun packChunkRelativeLocation(): Int {
        require(x in 0..15) { "X must be in range 0-15, got $x" }
        require(z in 0..15) { "Z must be in range 0-15, got $z" }
        require(y in -64..319) { "Y must be in range -64 to 319, got $y" }

        val yOffset = y + Y_OFFSET  // Convert to 0-383
        return (x and 0xF) or
                ((z and 0xF) shl 4) or
                ((yOffset and 0x1FF) shl 8)
    }

    fun packWithHalf(): Pair<Boolean, Short> {
        require(x in 0..15) { "X must be in range 0-15, got $x" }
        require(z in 0..15) { "Z must be in range 0-15, got $z" }
        require(y in -64..319) { "Y must be in range -64 to 319, got $y" }

        val isUpperHalf = isUpperHalf()

        // Calculate Y offset within its half (0-191)
        val yOffset = if (isUpperHalf) {
            y - UPPER_Y_MIN
        } else {
            y - LOWER_Y_MIN
        }

        require(yOffset in 0..255) { "Y offset out of range: $yOffset" }

        val packed = ((x and 0xF) or
                ((z and 0xF) shl 4) or
                ((yOffset and 0xFF) shl 8)).toShort()

        return Pair(isUpperHalf, packed)
    }
}