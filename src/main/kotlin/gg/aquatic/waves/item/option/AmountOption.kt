package gg.aquatic.waves.item.option

import org.bukkit.configuration.ConfigurationSection

object AmountOption : ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        if (section.contains("amount")) return null
        val amount = section.getInt("amount")
        ItemOptionHandle.create { }
        return ItemOptionHandle.create({ itemStack ->
            itemStack.amount = amount
        })
    }
}