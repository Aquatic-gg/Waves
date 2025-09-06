package gg.aquatic.waves.item.option

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.advancedplugins.ae.api.AEAPI
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import java.util.*

object EnchantsOption: ItemOption {
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

        return ItemOptionHandle.create({ item ->
            for ((ench, level) in enchantments) {
                if (ench.uppercase() == "AE-SLOTS") {
                    AEAPI.setTotalSlots(
                        item,
                        level
                    )
                    continue
                }
                if (ench.uppercase().startsWith("AE-")) {
                    AEAPI.applyEnchant(ench.substringBefore("AE-"), level, item)
                    continue
                }
                getEnchantmentByString(ench)?.apply {
                    item.addUnsafeEnchantment(this, level)
                }
            }
        }, { itemMeta ->
            if (itemMeta is EnchantmentStorageMeta) {
                for ((ench, level) in enchantments) {
                    if (ench.uppercase().startsWith("AE-")) continue
                    if (ench.uppercase() == "AE-SLOTS") continue

                    getEnchantmentByString(ench)?.apply {
                        itemMeta.addStoredEnchant(this, level, true)
                    }
                }
            }
        })
    }

    private fun getEnchantmentByString(ench: String): Enchantment? {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
            .get(NamespacedKey.minecraft(ench.lowercase(Locale.getDefault())))
    }
}