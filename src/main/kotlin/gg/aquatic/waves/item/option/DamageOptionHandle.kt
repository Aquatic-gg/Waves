package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class DamageOptionHandle(
    val damage: Int
): ItemOptionHandle {

    override val key = Companion.key
    override fun apply(itemStack: ItemStack) {
        itemStack.setData(DataComponentTypes.DAMAGE, damage)
    }

    companion object: ItemOption {
        override val key = Key.key("itemoption:damage")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            if (!section.contains("damage")) return null
            val damage = section.getInt("damage")
            return DamageOptionHandle(damage)
        }
    }

}