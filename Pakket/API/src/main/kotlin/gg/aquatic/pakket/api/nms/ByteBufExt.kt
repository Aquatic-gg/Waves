package gg.aquatic.pakket.api.nms

import io.netty.buffer.ByteBuf
import java.io.EOFException


fun ByteBuf.readVarInt(): Int {
    var value = 0
    var length = 0
    var currentByte: Byte
    do {
        currentByte = readByte()
        value = value or ((currentByte.toInt() and 0x7F) shl length * 7)
        length++
        if (length > 5) {
            throw RuntimeException("VarInt is too large. Must be smaller than 5 bytes.");
        }
    } while (currentByte.toInt() and 0x80 == 0x80)
    return value
}

fun ByteBuf.writeVarInt(value: Int) {
    if (value and (-0x1 shl 7) == 0) {
        writeByte(value)
    } else if (value and (-0x1 shl 14) == 0) {
        val w = (value and 0x7F or 0x80) shl 8 or (value ushr 7)
        writeShort(w)
    } else if (value and (-0x1 shl 21) == 0) {
        val w = (value and 0x7F or 0x80) shl 16 or (((value ushr 7) and 0x7F or 0x80) shl 8) or (value ushr 14)
        writeMedium(w)
    } else if (value and (-0x1 shl 28) == 0) {
        val w = ((value and 0x7F or 0x80) shl 24 or (((value ushr 7) and 0x7F or 0x80) shl 16)
                or (((value ushr 14) and 0x7F or 0x80) shl 8) or (value ushr 21))
        writeInt(w)
    } else {
        val w = (value and 0x7F or 0x80) shl 24 or (((value ushr 7) and 0x7F or 0x80) shl 16
                ) or (((value ushr 14) and 0x7F or 0x80) shl 8) or ((value ushr 21) and 0x7F or 0x80)
        writeInt(w)
        writeByte(value ushr 28)
    }
}

fun <K, V> ByteBuf.readMap(keyReader: Reader<K>, valueReader: Reader<V>): Map<K, V> {
    return readMap(keyReader, valueReader, Int.MAX_VALUE)
}

fun <K, V> ByteBuf.readMap(keyReader: Reader<K>, valueReader: Reader<V>, maxSize: Int): Map<K, V> {
    val size = this.readVarInt()
    if (size > maxSize) {
        throw java.lang.RuntimeException("$size elements exceeded max size of: $maxSize")
    }

    val map: MutableMap<K, V> = HashMap(size)
    for (i in 0..<size) {
        val key = keyReader.invoke(this)
        val value = valueReader.invoke(this)
        map.put(key, value)
    }
    return map
}
fun <K,V> ByteBuf.writeMap(map: Map<K,V>, keyWriter: Writer<K>, valueWriter: Writer<V>) {
    writeVarInt(map.size)
    for (entry in map.entries) {
        val key = entry.key
        val value = entry.value
        keyWriter(this, key)
        valueWriter(this, value)
    }
}
fun ByteBuf.readLongs(size: Int): LongArray {
    val array = LongArray(size)

    for (i in array.indices) {
        checkAvailable(8)
        array[i] = readLong()
    }
    return array
}

fun ByteBuf.readLongs(array: LongArray): Int {
    return readLongs(array, 0, array.size)
}

fun ByteBuf.readLongs(array: LongArray, offset: Int, length: Int): Int {
    for (index in offset..<offset + length) {
        try {
            array[index] = this.readLong()
        } catch (e: java.lang.Exception) {
            return index - offset
        }
    }

    return length
}

fun ByteBuf.endIndex(): Int {
    val readerIndex = this.readerIndex()
    val readableBytes = this.readableBytes()
    return readerIndex + readableBytes
}

fun ByteBuf.available(): Int {
    return endIndex() - readerIndex()
}

private fun ByteBuf.checkAvailable(fieldSize: Int) {
    if (fieldSize < 0) {
        throw IndexOutOfBoundsException("fieldSize cannot be a negative number")
    } else if (fieldSize > this.available()) {
        val value: Int = this.available()
        val msg = "fieldSize is too long! Length is " + fieldSize + ", but maximum is " + value
        if (value == 0) {
            throw Exception(msg)
        } else {
            throw EOFException(msg)
        }
    }
}

typealias Reader<T> = (ByteBuf) -> T
typealias Writer<T> = (ByteBuf, T) -> Unit
