package gg.aquatic.waves.item.factory

import gg.aquatic.waves.item.ItemHandler
import io.lumine.mythic.api.MythicProvider
import io.lumine.mythic.bukkit.adapters.BukkitItemStack
import org.bukkit.inventory.ItemStack

object MMFactory: ItemHandler.Factory {
    override fun create(id: String): ItemStack? {
        return (MythicProvider.get().itemManager.getItem(id).get()
            .generateItemStack(1) as BukkitItemStack).itemStack
    }
}