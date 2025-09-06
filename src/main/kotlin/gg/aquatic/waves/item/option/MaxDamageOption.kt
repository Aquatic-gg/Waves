package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

object MaxDamageOption: ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        if (!section.contains("max-damage")) return null
        val maxDamage = section.getInt("max-damage")
        return ItemOptionHandle.create({ item: ItemStack ->
            item.setData(DataComponentTypes.MAX_DAMAGE, maxDamage)
        })
    }
}