package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack

class RarityOptionHandle(
    val rarity: ItemRarity
): ItemOptionHandle {
    override val key = Companion.key
    override fun apply(itemStack: ItemStack) {
        itemStack.setData(DataComponentTypes.RARITY, rarity)
    }
    companion object: ItemOption {
        override val key = Key.key("itemoption:rarity")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            val rarityId = section.getString("rarity") ?: return null

            val rarity = try {
                ItemRarity.valueOf(rarityId.uppercase())
            } catch (_: IllegalArgumentException) {
                return null
            }
            return RarityOptionHandle(rarity)
        }
    }
}