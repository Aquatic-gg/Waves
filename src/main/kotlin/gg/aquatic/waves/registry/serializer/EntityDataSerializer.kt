package gg.aquatic.waves.registry.serializer

import gg.aquatic.waves.fake.entity.data.ConfiguredEntityData
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.registry.entityData
import gg.aquatic.waves.util.argument.ArgumentSerializer
import gg.aquatic.waves.util.argument.ObjectArguments
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType

object EntityDataSerializer {

    fun load(section: ConfigurationSection, entityType: EntityType? = null): List<ConfiguredEntityData> {
        val data = ArrayList<ConfiguredEntityData>()
        val entityData = if (entityType == null) {
            val map = HashMap<String, EntityData>()
            WavesRegistry.ENTITY_DATA.forEach { (_, value) ->
                map += value
            }
            map
        } else WavesRegistry.entityData(entityType)
        for (key in section.getKeys(false)) {
            val type = entityData[key] ?: continue
            val arguments = ObjectArguments(ArgumentSerializer.load(section, type.arguments))
            data += ConfiguredEntityData(type, arguments)
        }
        return data
    }

}