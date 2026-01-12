package gg.aquatic.blokk.impl

import gg.aquatic.blokk.Blokk
import gg.aquatic.common.toBlockCardinal
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.structure.StructureRotation

class VanillaBlock(
    override val blockData: BlockData,
): Blokk() {
    override fun place(location: Location) {

        val cardinal = location.yaw.toBlockCardinal()
        val rotation = when (cardinal) {
            BlockFace.NORTH -> StructureRotation.NONE
            BlockFace.EAST -> StructureRotation.CLOCKWISE_90
            BlockFace.SOUTH -> StructureRotation.CLOCKWISE_180
            BlockFace.WEST -> StructureRotation.COUNTERCLOCKWISE_90
            else -> StructureRotation.NONE
        }

        val rotatedData = blockData.clone().apply { rotate(rotation) }

        location.block.type = rotatedData.material
        location.block.blockData = rotatedData
    }
}