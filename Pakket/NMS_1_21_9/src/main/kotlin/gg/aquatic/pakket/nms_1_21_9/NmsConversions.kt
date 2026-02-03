package gg.aquatic.pakket.nms_1_21_9

import com.google.gson.JsonParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.chat.ComponentSerialization
import com.mojang.serialization.JsonOps
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

object NmsConversions {

    fun toNmsComponent(component: Component): net.minecraft.network.chat.Component {
        val kyoriJson = GsonComponentSerializer.gson().serialize(component)
        return ComponentSerialization.CODEC.parse(
            JsonOps.INSTANCE,
            JsonParser.parseString(kyoriJson)
        ).orThrow
    }

    fun toNmsItemStack(itemStack: ItemStack): net.minecraft.world.item.ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }
}
