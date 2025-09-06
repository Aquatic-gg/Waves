package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class MaxDamageOptionHandle(
    val maxDamage: Int
): ItemOptionHandle {

    override val key = Companion.key
    override fun apply(itemStack: ItemStack) {
        itemStack.setData(DataComponentTypes.MAX_DAMAGE, maxDamage)
    }

    companion object: ItemOption {
        override val key = Key.key("itemoption:max-damage")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            if (!section.contains("max-damage")) return null
            val maxDamage = section.getInt("max-damage")
            return MaxDamageOptionHandle(maxDamage)
        }
    }

}