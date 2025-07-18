package gg.aquatic.waves.item.factory

import com.nexomc.nexo.api.NexoItems
import gg.aquatic.waves.item.ItemHandler
import org.bukkit.inventory.ItemStack

object NexoFactory: ItemHandler.Factory {
    override fun create(id: String): ItemStack? {
        return NexoItems.itemFromId(id)?.build()
    }
}