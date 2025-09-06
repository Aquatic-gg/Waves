package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

object MaxStackSizeOption: ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        if (!section.contains("max-stack-size")) return null
        val maxStackSize = section.getInt("max-stack-size")
        return ItemOptionHandle.create({ item: ItemStack ->
            item.setData(DataComponentTypes.MAX_STACK_SIZE, maxStackSize)
        })
    }
}