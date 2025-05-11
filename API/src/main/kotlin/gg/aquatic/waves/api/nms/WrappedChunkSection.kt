package gg.aquatic.waves.api.nms

import org.bukkit.block.data.BlockData

interface WrappedChunkSection {

    fun set(x: Int, y: Int, z: Int, blockState: BlockData)
    fun get(x: Int, y: Int, z: Int): BlockData

}