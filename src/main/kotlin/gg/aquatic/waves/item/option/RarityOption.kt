package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack

object RarityOption: ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        val rarityId = section.getString("rarity") ?: return null
        val rarity = try {
            ItemRarity.valueOf(rarityId.uppercase())
        } catch (_: IllegalArgumentException) {
            return null
        }
        return ItemOptionHandle.create({ item: ItemStack ->
            item.setData(DataComponentTypes.RARITY, rarity)
        })
    }
}