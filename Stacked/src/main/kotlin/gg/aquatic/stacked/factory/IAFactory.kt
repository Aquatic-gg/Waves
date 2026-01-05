package gg.aquatic.stacked.factory

import dev.lone.itemsadder.api.CustomStack
import gg.aquatic.stacked.ItemHandler
import org.bukkit.inventory.ItemStack

object IAFactory: ItemHandler.Factory {
    override fun create(id: String): ItemStack? {
        return CustomStack.getInstance(id)!!.itemStack
    }
}