package gg.aquatic.stacked.factory

import com.willfp.eco.core.items.Items
import com.willfp.eco.core.recipe.parts.EmptyTestableItem
import gg.aquatic.stacked.ItemFactory
import gg.aquatic.stacked.ItemHandler
import org.bukkit.inventory.ItemStack

object EcoFactory: ItemFactory {
    override fun create(id: String): ItemStack? {
        val lookup = Items.lookup(id)
        if (lookup is EmptyTestableItem) {
            return null
        }
        return lookup.item
    }
}