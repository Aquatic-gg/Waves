package gg.aquatic.stacked

import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder

object ItemEncoder {

    fun encode(itemStack: ItemStack): ByteArray {
        return itemStack.ensureServerConversions().serializeAsBytes()
    }

    fun decode(base64: ByteArray): ItemStack {
        return ItemStack.deserializeBytes(base64)
    }


    fun encodeItems(items: List<ItemStack>): String {
        return String(ItemStack.serializeItemsAsBytes(items.map { it.ensureServerConversions() }), Charsets.UTF_8)
    }

    fun decodeItems(base64: String): List<ItemStack> {
        return ItemStack.deserializeItemsFromBytes(Base64Coder.decode(base64)).toList()
    }


}