package gg.aquatic.waves.item.option

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

interface ItemOptionHandle {
    fun apply(itemStack: ItemStack)
    fun apply(itemMeta: ItemMeta)

    companion object {
        fun create(itemApply: (ItemStack) -> Unit = {}, metaApply: (ItemMeta) -> Unit = {}): ItemOptionHandle {
            return object : ItemOptionHandle {
                override fun apply(itemStack: ItemStack) {
                    apply(itemStack)
                }

                override fun apply(itemMeta: ItemMeta) {
                    apply(itemMeta)
                }
            }
        }
    }
}