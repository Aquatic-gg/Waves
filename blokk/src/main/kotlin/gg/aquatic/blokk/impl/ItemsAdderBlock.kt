package gg.aquatic.blokk.impl

import dev.lone.itemsadder.api.CustomBlock
import gg.aquatic.blokk.Blokk
import org.bukkit.Location
import org.bukkit.block.data.BlockData

class ItemsAdderBlock(
    val iaId: String
): Blokk() {
    override fun place(location: Location) {
        val customBlock: CustomBlock = CustomBlock.getInstance(iaId) ?: return
        customBlock.place(location)
    }

    override val blockData: BlockData
        get() {
            val customBlock: CustomBlock = CustomBlock.getInstance(iaId)!!
            return customBlock.block.blockData
        }
}