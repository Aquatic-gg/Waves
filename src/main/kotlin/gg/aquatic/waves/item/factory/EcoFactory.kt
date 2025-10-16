package gg.aquatic.waves.item.factory

import com.willfp.eco.core.items.Items
import com.willfp.eco.core.recipe.parts.EmptyTestableItem
import gg.aquatic.waves.item.ItemHandler
import org.bukkit.inventory.ItemStack

object EcoFactory: ItemHandler.Factory {
    override fun create(id: String): ItemStack? {
        val lookup = Items.lookup(id)
        if (lookup is EmptyTestableItem) {
            return null
        }
        return lookup.item
    }
}