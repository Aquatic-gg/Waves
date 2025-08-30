package gg.aquatic.waves.util.argument.impl

import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import org.bukkit.configuration.ConfigurationSection

class PrimitiveObjectArgument(
    id: String,
    defaultValue: Any?,
    required: Boolean,
    aliases: Collection<String> = listOf(),
) : AquaticObjectArgument<Any?>(
    id, defaultValue,
    required,
    aliases
) {
    override val serializer: AbstractObjectArgumentSerializer<Any?>
        get() {
            return Serializer
        }

    object Serializer : AbstractObjectArgumentSerializer<Any?>() {
        override fun load(section: ConfigurationSection, id: String): Any? {
            return section.get(id)
        }
    }
}