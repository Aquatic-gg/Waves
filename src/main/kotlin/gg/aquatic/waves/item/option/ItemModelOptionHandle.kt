package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class ItemModelOptionHandle(val model: Key): ItemOptionHandle {

    override val key = Companion.key
    override fun apply(itemStack: ItemStack) {
        itemStack.setData(DataComponentTypes.ITEM_MODEL, model)
    }

    companion object: ItemOption {
        override val key = Key.key("itemoption:item-model")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            val model = section.getString("item-model") ?: return null
            return ItemModelOptionHandle(Key.key(model))
        }
    }

}