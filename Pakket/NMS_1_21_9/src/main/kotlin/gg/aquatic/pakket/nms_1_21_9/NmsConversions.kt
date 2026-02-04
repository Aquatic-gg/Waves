package gg.aquatic.pakket.nms_1_21_9

import io.papermc.paper.adventure.AdventureComponent
import net.kyori.adventure.text.Component
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

object NmsConversions {

    fun toNmsComponent(component: Component): net.minecraft.network.chat.Component {
        return AdventureComponent(component)
    }

    fun toNmsItemStack(itemStack: ItemStack): net.minecraft.world.item.ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }
}
