package gg.aquatic.waves.item.option

import gg.aquatic.waves.util.item.setSpawnerType
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.inventory.meta.ItemMeta

class SpawnerTypeOptionHandle(
    val entityType: EntityType
): ItemOptionHandle {

    override val key = Companion.key
    override fun apply(itemMeta: ItemMeta) {
        itemMeta.setSpawnerType(entityType)
    }
    companion object: ItemOption {
        override val key = Key.key("itemoption:spawner-type")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            val entityType = section.getString("entity-type") ?: return null
            val entity = try {
                EntityType.valueOf(entityType.uppercase())
            } catch (_: IllegalArgumentException) {
                return null
            }
            return SpawnerTypeOptionHandle(entity)
        }
    }
}