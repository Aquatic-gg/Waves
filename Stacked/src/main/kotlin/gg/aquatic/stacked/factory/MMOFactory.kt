package gg.aquatic.stacked.factory

import gg.aquatic.stacked.ItemFactory
import gg.aquatic.stacked.ItemHandler
import net.Indyuce.mmoitems.MMOItems
import org.bukkit.inventory.ItemStack

object MMOFactory: ItemFactory {
    override fun create(id: String): ItemStack? {
        val args = id.split(":")
        return MMOItems.plugin.getItem(args[0], args[1])
    }
}