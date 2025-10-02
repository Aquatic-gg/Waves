package gg.aquatic.waves.item.option

import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

class FlagsOptionHandle(
    val flags: List<ItemFlag>,
) : ItemOptionHandle {

    override val key = Companion.key

    override fun apply(itemMeta: ItemMeta) {
        itemMeta.addItemFlags(*flags.toTypedArray())
    }

    companion object : ItemOption {
        override val key = Key.key("itemoption:flags")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            if (!section.contains("flags")) return null
            val flags = section.getStringList("flags").mapNotNull { ItemFlag.valueOf(it.uppercase()) }
            if (flags.isEmpty()) return null
            return FlagsOptionHandle(flags)
        }
    }

}