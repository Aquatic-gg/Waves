package gg.aquatic.blokk.impl

import gg.aquatic.blokk.AquaticBlock
import org.bukkit.Location
import org.bukkit.block.data.BlockData

class VanillaBlock(
    override val blockData: BlockData,
): AquaticBlock() {
    override fun place(location: Location) {
        location.block.type = blockData.material
        location.block.blockData = blockData
    }
}