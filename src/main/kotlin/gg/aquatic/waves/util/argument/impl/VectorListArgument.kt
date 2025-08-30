package gg.aquatic.waves.util.argument.impl

import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector

class VectorListArgument(id: String, defaultValue: List<Vector>?, required: Boolean) : AquaticObjectArgument<List<Vector>>(id, defaultValue,
    required
) {
    override val serializer: AbstractObjectArgumentSerializer<List<Vector>?> = Serializer

    override fun load(section: ConfigurationSection): List<Vector>? {
        return Serializer.load(section, id)
    }

    object Serializer: AbstractObjectArgumentSerializer<List<Vector>?>() {
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