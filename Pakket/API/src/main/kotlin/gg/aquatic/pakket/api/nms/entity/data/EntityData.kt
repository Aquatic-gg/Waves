package gg.aquatic.pakket.api.nms.entity.data

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArguments
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import org.bukkit.entity.Entity

interface EntityData {

    val id: String
    val entityClass: Class<out Entity>

    fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue>
    val arguments: List<ObjectArgument<*>>

}