package gg.aquatic.stacked.factory

import com.nexomc.nexo.api.NexoItems
import gg.aquatic.stacked.ItemFactory
import gg.aquatic.stacked.ItemHandler
import org.bukkit.inventory.ItemStack

object NexoFactory: ItemFactory {
    override fun create(id: String): ItemStack? {
        return NexoItems.itemFromId(id)?.build()
    }
}