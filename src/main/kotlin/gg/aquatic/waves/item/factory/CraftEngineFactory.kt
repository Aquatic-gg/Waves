package gg.aquatic.waves.item.factory

import gg.aquatic.waves.item.ItemHandler
import net.momirealms.craftengine.bukkit.api.CraftEngineItems
import net.momirealms.craftengine.core.util.Key
import org.bukkit.inventory.ItemStack

object CraftEngineFactory : ItemHandler.Factory {
    override fun create(id: String): ItemStack? {
        val item = CraftEngineItems.byId(Key.of(id)) ?: return null
        return item.buildItemStack()
    }
}