package gg.aquatic.waves.registry.serializer

import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.util.keysForEach
import org.bukkit.configuration.ConfigurationSection

object EntityPropertySerializer {

    fun fromSection(section: ConfigurationSection): MutableList<EntityProperty> {
        val properties = mutableListOf<EntityProperty>()
        section.keysForEach(false) {
            val factory = WavesRegistry.ENTITY_PROPERTY_FACTORIES[it]
            factory?.serialize(section)?.let { property ->
                properties.add(property)
            }
        }
        return properties
    }

}