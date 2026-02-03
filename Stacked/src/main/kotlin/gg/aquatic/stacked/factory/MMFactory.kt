package gg.aquatic.stacked.factory

import gg.aquatic.stacked.ItemFactory
import gg.aquatic.stacked.ItemHandler
import io.lumine.mythic.api.MythicProvider
import io.lumine.mythic.bukkit.adapters.BukkitItemStack
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

object MMFactory: ItemFactory {
    override fun create(id: String): ItemStack? {
        return (MythicProvider.get().itemManager.getItem(id).getOrNull()
            ?.generateItemStack(1) as BukkitItemStack?)?.itemStack
    }
}