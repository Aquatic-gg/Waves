package gg.aquatic.waves.util.item

import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder

object ItemEncoder {

    fun encode(itemStack: ItemStack): String {
        return String(itemStack.serializeAsBytes(), Charsets.UTF_8)
    }

    fun decode(base64: String): ItemStack {
        return ItemStack.deserializeBytes(Base64Coder.decode(base64))
    }

    /*
    fun encodeItems(items: List<ItemStack>): String {
        try {
            ByteArrayOutputStream().use { outputStream ->
                BukkitObjectOutputStream(outputStream).use { dataOutput ->
                    dataOutput.writeObject(items)
                    return String(Base64Coder.encode(outputStream.toByteArray()))
                }
            }
        } catch (exception: Exception) {
            throw exception
        }
    }

    fun decodeItems(base64: String): List<ItemStack> {
        try {
            ByteArrayInputStream(Base64Coder.decode(base64)).use { inputStream ->
                BukkitObjectInputStream(inputStream).use { dataInput ->
                    return (dataInput.readObject() as List<ItemStack>)
                }
            }
        } catch (exception: Exception) {
            throw exception
        }
    }
     */

}