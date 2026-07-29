package gg.aquatic.stacked

import org.bukkit.inventory.ItemStack

interface ItemFactory {
    fun create(id: String): ItemStack?
}