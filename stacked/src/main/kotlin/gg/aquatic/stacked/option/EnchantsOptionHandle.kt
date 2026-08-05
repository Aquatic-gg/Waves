package gg.aquatic.stacked.option

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import java.util.*

class EnchantsOptionHandle(
    val enchants: Map<String, Int>,
) : ItemOptionHandle {

    override val key = Companion.key
    override fun apply(itemStack: ItemStack) {
        for ((ench, level) in enchants) {
            getEnchantmentByString(ench)?.apply {
                itemStack.addUnsafeEnchantment(this, level)
            }
        }
    }

    override fun apply(itemMeta: ItemMeta) {
        if (itemMeta is EnchantmentStorageMeta) {
            for ((ench, level) in enchants) {
                if (ench.uppercase().startsWith("AE-")) continue
                if (ench.uppercase() == "AE-SLOTS") continue

                getEnchantmentByString(ench)?.apply {
                    itemMeta.addStoredEnchant(this, level, true)
                }
            }
        }
    }

    private fun getEnchantmentByString(ench: String): Enchantment? {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
            .get(NamespacedKey.minecraft(ench.lowercase(Locale.getDefault())))
    }

    companion object : ItemOption {

        override val key = Key.key("itemoption:enchants")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            if (!section.contains("enchants")) return null

            val enchantments: MutableMap<String, Int> = HashMap()
            for (str in section.getStringList("enchants")) {
                val strs = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (strs.size < 2) {
                    continue
                }
                val enchantment = strs[0]
                val level = strs[1].toInt()
                enchantments[enchantment] = level
            }
            if (enchantments.isEmpty()) return null
            return EnchantsOptionHandle(enchantments)
        }
    }
}