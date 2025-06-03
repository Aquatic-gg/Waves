package gg.aquatic.waves.fake.entity.data

import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.util.argument.ObjectArguments

class ConfiguredEntityData(
    val entityData: EntityData,
    val arguments: ObjectArguments
) {

    fun generate(updater: (String) -> String): Collection<EntityDataValue> {
        return entityData.generate(arguments,updater)
    }
}