package gg.aquatic.clientside.entity.data

import gg.aquatic.common.argument.ObjectArguments
import gg.aquatic.pakket.api.nms.entity.EntityDataValue

class ConfiguredEntityData(
    val entityData: EntityData,
    val arguments: ObjectArguments
) {

    fun generate(updater: (String) -> String): Collection<EntityDataValue> {
        return entityData.generate(arguments,updater)
    }
}