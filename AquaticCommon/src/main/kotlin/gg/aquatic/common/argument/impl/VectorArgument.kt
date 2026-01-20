package gg.aquatic.common.argument.impl

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArgumentFactory
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector

class VectorArgument(id: String, defaultValue: Vector?, required: Boolean, aliases: Collection<String> = listOf()) : ObjectArgument<Vector>(id, defaultValue,
    required,
    aliases
) {
    override val serializer: ObjectArgumentFactory<Vector?> = Factory

    object Factory: ObjectArgumentFactory<Vector?>() {
        override fun load(section: ConfigurationSection, id: String): Vector? {
            val str = section.getString(id) ?: return null
            val split = str.split(";")
            if (split.size != 3) {
                return null
            }
            val vector = Vector(split[0].toDouble(), split[1].toDouble(), split[2].toDouble())
            return vector
        }
    }
}