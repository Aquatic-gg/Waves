package gg.aquatic.waves.registry

import gg.aquatic.waves.fake.entity.data.EntityClassLookup
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.util.logger.type.InfoLogger
import org.bukkit.entity.EntityType
import java.util.concurrent.ConcurrentHashMap

fun WavesRegistry.registerEntityData(namespace: String) {
    val entityDataClasses = EntityClassLookup.searchForEntityDataClasses(namespace)
    entityDataClasses.forEach {
        val entityData = EntityClassLookup.createEntityDataInstance(it)
        if (entityData != null) {
            val map = ENTITY_DATA.getOrPut(entityData.entityClass) { ConcurrentHashMap() }
            map += entityData.id to entityData
            InfoLogger.send("Registered EntityData with ID: ${entityData.id}, type: ${entityData.entityClass.simpleName}")
        }
    }
}

fun WavesRegistry.entityData(entityType: EntityType): Map<String, EntityData> {
    val data = HashMap<String, EntityData>()
    val entityClazz = entityType.entityClass ?: return data
    for ((key,map) in ENTITY_DATA) {
        if (entityClazz == key || entityClazz.isAssignableFrom(key)) {
            data += map
        }
    }
    return data
}