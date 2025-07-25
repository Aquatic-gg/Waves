package gg.aquatic.waves.item

import com.google.common.collect.HashMultimap
import gg.aquatic.waves.util.item.setSpawnerType
import gg.aquatic.waves.util.toMMComponent
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.advancedplugins.ae.api.AEAPI
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import java.util.*

class AquaticItem(
    val internalId: String? = null,
    private val item: ItemStack,
    val name: String?,
    val description: MutableList<String>?,
    val amount: Int,
    val modelData: Int,
    val itemModel: String?,
    val enchantments: MutableMap<String, Int>?,
    val flags: MutableList<ItemFlag>?,
    val spawnerEntityType: EntityType?,
) {

    fun giveItem(player: Player) {
        giveItem(player, amount)
    }

    fun giveItem(player: Player, amount: Int) {
        val iS = getItem()
        iS.amount = amount

        player.inventory.addItem(iS)
    }

    fun getUnmodifiedItem(): ItemStack {
        return item
    }

    fun getItem(): ItemStack {
        val iS = getUnmodifiedItem()

        val im = iS.itemMeta ?: return iS
        name?.apply {
            im.displayName(this.toMMComponent().decoration(TextDecoration.ITALIC, false))
        }
        description?.apply {
            im.lore(this.map { it.toMMComponent().decoration(TextDecoration.ITALIC, false) })
        }
        if (this@AquaticItem.modelData > 0) {
            im.setCustomModelData(this@AquaticItem.modelData)
        }

        spawnerEntityType?.apply {
            if (iS.type == Material.SPAWNER) {
                im.setSpawnerType(this)
            }
        }

        val modifiers = im.attributeModifiers
        if (modifiers == null) {
            im.attributeModifiers = HashMultimap.create()
        }

        flags?.apply {
            im.addItemFlags(*this.toTypedArray())
        }

        if (enchantments != null) {
            if (iS.type == Material.ENCHANTED_BOOK) {
                val esm = im as EnchantmentStorageMeta
                for ((ench, level) in enchantments) {
                    if (ench.uppercase().startsWith("AE-")) continue
                    if (ench.uppercase() == "AE-SLOTS") continue

                    getEnchantmentByString(ench)?.apply {
                        esm.addStoredEnchant(this, level, true)
                    }
                }
                iS.itemMeta = esm
            } else {
                iS.itemMeta = im
                for ((ench, level) in enchantments) {
                    if (ench.uppercase() == "AE-SLOTS") {
                        AEAPI.setTotalSlots(
                            iS,
                            level
                        )
                        continue
                    }
                    if (ench.uppercase().startsWith("AE-")) {
                        AEAPI.applyEnchant(ench.substringBefore("AE-"), level, iS)
                        continue
                    }
                    getEnchantmentByString(ench)?.apply {
                        iS.addUnsafeEnchantment(this, level)
                    }
                }
            }
        } else iS.itemMeta = im

        iS.amount = amount
        if (itemModel != null) {
            iS.setData(DataComponentTypes.ITEM_MODEL, Key.key(itemModel))
        }
        return iS
    }

    companion object {
        private fun getEnchantmentByString(ench: String): Enchantment? {
            return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
                .get(NamespacedKey.minecraft(ench.lowercase(Locale.getDefault())))
        }

        /*
        fun get(id: String): AquaticItem? {
            return ItemHandler.itemRegistry[id]
        }

        fun get(itemStack: ItemStack): AquaticItem? {
            val pdc = itemStack.itemMeta?.persistentDataContainer ?: return null
            if (!pdc.has(ItemHandler.NAMESPACE_KEY, PersistentDataType.STRING)) return null
            val id = pdc.get(ItemHandler.NAMESPACE_KEY, PersistentDataType.STRING)
            return ItemHandler.itemRegistry[id]
        }
         */
    }
}