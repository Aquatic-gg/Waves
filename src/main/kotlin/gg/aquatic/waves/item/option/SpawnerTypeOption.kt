package gg.aquatic.waves.item.option

import gg.aquatic.waves.util.item.setSpawnerType
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.inventory.meta.ItemMeta

object SpawnerTypeOption: ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        val entityType = section.getString("entity-type") ?: return null
        val entity = try {
            EntityType.valueOf(entityType.uppercase())
        } catch (_: IllegalArgumentException) {
            return null
        }
        return ItemOptionHandle.create { itemMeta: ItemMeta ->
            itemMeta.setSpawnerType(entity)
        }
    }
}