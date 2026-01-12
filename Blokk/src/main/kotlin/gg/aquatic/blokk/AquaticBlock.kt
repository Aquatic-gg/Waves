package gg.aquatic.blokk

import org.bukkit.Location
import org.bukkit.block.data.BlockData

abstract class AquaticBlock {

    abstract fun place(location: Location)
    abstract val blockData: BlockData

}