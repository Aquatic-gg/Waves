package gg.aquatic.blokk.impl

import gg.aquatic.blokk.Blokk
import io.th0rgal.oraxen.api.OraxenBlocks
import org.bukkit.Location
import org.bukkit.block.data.BlockData

class OraxenBlock(
    val oraxenId: String
): Blokk() {
    override fun place(location: Location) {
        OraxenBlocks.place(oraxenId,location)
    }

    override val blockData: BlockData
        get() {
            return OraxenBlocks.getOraxenBlockData(oraxenId)!!
        }
}