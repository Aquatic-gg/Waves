package gg.aquatic.blokk.impl

import gg.aquatic.blokk.Blokk
import org.bukkit.Location
import org.bukkit.block.data.BlockData

class VanillaBlock(
    override val blockData: BlockData,
): Blokk() {
    override fun place(location: Location) {
        location.block.type = blockData.material
        location.block.blockData = blockData
    }
}