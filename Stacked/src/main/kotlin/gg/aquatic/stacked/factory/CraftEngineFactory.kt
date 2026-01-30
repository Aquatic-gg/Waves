package gg.aquatic.stacked.factory

import gg.aquatic.stacked.ItemFactory
import gg.aquatic.stacked.ItemHandler
import net.momirealms.craftengine.bukkit.api.CraftEngineItems
import net.momirealms.craftengine.core.util.Key
import org.bukkit.inventory.ItemStack

object CraftEngineFactory : ItemFactory {
    override fun create(id: String): ItemStack? {
        val item = CraftEngineItems.byId(Key.of(id)) ?: return null
        return item.buildItemStack()
    }
}