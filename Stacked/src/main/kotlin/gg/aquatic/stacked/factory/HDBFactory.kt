package gg.aquatic.stacked.factory

import gg.aquatic.stacked.ItemFactory
import me.arcaniax.hdb.api.HeadDatabaseAPI
import org.bukkit.inventory.ItemStack

object HDBFactory: ItemFactory {
    override fun create(id: String): ItemStack? {
        return HeadDatabaseAPI().getItemHead(id)
    }
}