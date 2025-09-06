package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

object ItemModelOption : ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        val modelData = section.getString("item-model") ?: return null
        return ItemOptionHandle.create({ item: ItemStack ->
            item.setData(DataComponentTypes.ITEM_MODEL, Key.key(modelData))
        })
    }
}