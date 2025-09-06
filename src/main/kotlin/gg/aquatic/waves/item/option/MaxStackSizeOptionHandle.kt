package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class MaxStackSizeOptionHandle(
    val maxStackSize: Int
): ItemOptionHandle {

    override val key = Companion.key
    override fun apply(itemStack: ItemStack) {
        itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, maxStackSize)
    }

    companion object: ItemOption {
        override val key = Key.key("itemoption:max-stack-size")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            if (!section.contains("max-stack-size")) return null
            val maxStackSize = section.getInt("max-stack-size")
            return MaxStackSizeOptionHandle(maxStackSize)
        }
    }
}