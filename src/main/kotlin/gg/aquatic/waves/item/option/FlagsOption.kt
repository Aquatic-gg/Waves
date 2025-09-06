package gg.aquatic.waves.item.option

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

object FlagsOption: ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        if (!section.contains("flags")) return null
        val flags: MutableList<ItemFlag> = ArrayList()
        for (flag in section.getStringList("flags")) {
            val itemFlag = ItemFlag.valueOf(flag.uppercase())
            flags.add(itemFlag)
        }

        return ItemOptionHandle.create { itemMeta: ItemMeta ->
            itemMeta.addItemFlags(*flags.toTypedArray())
        }
    }
}