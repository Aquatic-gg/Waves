package gg.aquatic.waves.util.argument.impl

import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector

class VectorArgument(id: String, defaultValue: Vector?, required: Boolean) : AquaticObjectArgument<Vector>(id, defaultValue,
    required
) {
    override val serializer: AbstractObjectArgumentSerializer<Vector?> = Serializer

    override fun load(section: ConfigurationSection): Vector? {
        return Serializer.load(section, id) ?: defaultValue
    }

    object Serializer: AbstractObjectArgumentSerializer<Vector?>() {
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