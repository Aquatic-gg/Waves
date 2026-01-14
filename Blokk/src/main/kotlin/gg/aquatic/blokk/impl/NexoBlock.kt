package gg.aquatic.blokk.impl

import com.nexomc.nexo.api.NexoBlocks
import gg.aquatic.blokk.Blokk
import org.bukkit.Location
import org.bukkit.block.data.BlockData

class NexoBlock(
    val nexoId: String
): Blokk() {
    override fun place(location: Location) {
        NexoBlocks.place(nexoId,location)
    }

    override val blockData: BlockData
        get() {
            return NexoBlocks.blockData(nexoId)!!
        }
}