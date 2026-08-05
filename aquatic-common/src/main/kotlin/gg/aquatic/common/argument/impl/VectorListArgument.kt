package gg.aquatic.common.argument.impl

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArgumentFactory
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector

class VectorListArgument(id: String, defaultValue: List<Vector>?, required: Boolean, aliases: Collection<String> = listOf()) : ObjectArgument<List<Vector>>(id, defaultValue,
    required,
    aliases
) {
    override val serializer: ObjectArgumentFactory<List<Vector>?> = Factory

    object Factory: ObjectArgumentFactory<List<Vector>?>() {
        override fun load(section: ConfigurationSection, id: String): List<Vector> {
            val strs = section.getStringList(id)
            val vectors = mutableListOf<Vector>()
            for (str in strs) {
                val split = str.split(";")
                if (split.size != 3) {
                    continue
                }
                val vector = Vector(split[0].toDouble(), split[1].toDouble(), split[2].toDouble())
                vectors.add(vector)
            }

            return vectors
        }
    }
}