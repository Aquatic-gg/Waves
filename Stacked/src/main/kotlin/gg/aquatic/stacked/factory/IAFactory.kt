package gg.aquatic.stacked.factory

import dev.lone.itemsadder.api.CustomStack
import gg.aquatic.stacked.ItemFactory
import gg.aquatic.stacked.ItemHandler
import org.bukkit.inventory.ItemStack

object IAFactory: ItemFactory {
    override fun create(id: String): ItemStack? {
        return CustomStack.getInstance(id)!!.itemStack
    }
}