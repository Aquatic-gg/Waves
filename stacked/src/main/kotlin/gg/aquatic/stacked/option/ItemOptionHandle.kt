package gg.aquatic.stacked.option

import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

interface ItemOptionHandle {
    val key: Key

    fun apply(itemStack: ItemStack) {}
    fun apply(itemMeta: ItemMeta) {}
}