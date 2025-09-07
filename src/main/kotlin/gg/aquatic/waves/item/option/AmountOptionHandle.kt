package gg.aquatic.waves.item.option

import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class AmountOptionHandle(
    val amount: Int
): ItemOptionHandle {

    override val key = Companion.key

    override fun apply(itemStack: ItemStack) {
        itemStack.amount = amount
    }

    companion object: ItemOption {
        override val key = Key.key("itemoption:amount")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            if (!section.contains("amount")) return null
            val amount = section.getInt("amount")
            return AmountOptionHandle(amount)
        }

    }
}