package gg.aquatic.waves.registry

import gg.aquatic.waves.fake.entity.data.EntityClassLookup

fun WavesRegistry.registerEntityData(namespace: String) {
    val entityDataClasses = EntityClassLookup.searchForEntityDataClasses(namespace)
    entityDataClasses.forEach {
        val entityData = EntityClassLookup.createEntityDataInstance(it)
        if (entityData != null) {
            ENTITY_DATA[entityData.id] = entityData
        }
    }
}