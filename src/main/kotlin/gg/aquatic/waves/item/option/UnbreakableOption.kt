package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Unbreakable
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

object UnbreakableOption : ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        if (!section.contains("unbreakable")) return null
        val unbreakable = section.getBoolean("unbreakable")
        if (!unbreakable) return null
        return ItemOptionHandle.create({ item: ItemStack ->
            item.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false))
        })
    }
}