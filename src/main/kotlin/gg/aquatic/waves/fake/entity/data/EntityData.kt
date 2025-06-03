package gg.aquatic.waves.fake.entity.data

import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments

interface EntityData {

    val id: String

    fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue>
    val arguments: List<AquaticObjectArgument<*>>

}