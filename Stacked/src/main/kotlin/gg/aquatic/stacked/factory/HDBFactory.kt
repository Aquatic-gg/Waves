package gg.aquatic.stacked.factory

import gg.aquatic.stacked.ItemHandler
import me.arcaniax.hdb.api.HeadDatabaseAPI
import org.bukkit.inventory.ItemStack

object HDBFactory: ItemHandler.Factory {
    override fun create(id: String): ItemStack? {
        return HeadDatabaseAPI().getItemHead(id)
    }
}