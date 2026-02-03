package gg.aquatic.stacked.factory

import gg.aquatic.stacked.ItemEncoder
import gg.aquatic.stacked.ItemFactory
import gg.aquatic.stacked.ItemHandler
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder

object Base64Factory: ItemFactory {
    override fun create(id: String): ItemStack? {
        return try {
            ItemEncoder.decode(Base64Coder.decode(id))
        } catch (_: Exception) {
            null
        }
    }
}