package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

object DamageOption: ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        if (!section.contains("damage")) return null
        val damage = section.getInt("damage")

        return ItemOptionHandle.create( { item: ItemStack ->
            item.setData(DataComponentTypes.DAMAGE, damage)
        })
    }
}