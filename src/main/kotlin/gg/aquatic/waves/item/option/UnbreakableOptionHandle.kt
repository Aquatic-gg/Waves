package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Unbreakable
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class UnbreakableOptionHandle: ItemOptionHandle {

    override val key = Companion.key
    override fun apply(itemStack: ItemStack) {
        itemStack.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false))
    }

    companion object: ItemOption {
        override val key = Key.key("itemoption:unbreakable")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            val unbreakable = section.getBoolean("unbreakable",false)
            if (!unbreakable) return null
            return UnbreakableOptionHandle()
        }
    }
}