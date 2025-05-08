package gg.aquatic.waves.api.nms.chunk

import org.bukkit.block.data.BlockData

interface WrappedChunkSection {

    fun set(x: Int, y: Int, z: Int, blockState: BlockData)
    fun get(x: Int, y: Int, z: Int): BlockData

}