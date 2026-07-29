package gg.aquatic.pakket.api.nms

import org.bukkit.block.data.BlockData

interface WrappedChunkSection {

    val section: Any

    fun set(x: Int, y: Int, z: Int, blockState: BlockData)
    fun get(x: Int, y: Int, z: Int): BlockData

}