package gg.aquatic.pakket.api.nms

import org.bukkit.Location
import org.bukkit.util.Vector

data class BlockPos(
    val x: Int,
    val y: Int,
    val z: Int
)

fun Location.toBlockPos(): BlockPos {
    return BlockPos(blockX, blockY, blockZ)
}
fun Vector.toBlockPos(): BlockPos {
    return BlockPos(blockX, blockY, blockZ)
}