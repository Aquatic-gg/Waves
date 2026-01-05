package gg.aquatic.execute.argument.impl

import gg.aquatic.execute.argument.ObjectArgumentFactory
import gg.aquatic.execute.argument.ObjectArgument
import org.bukkit.configuration.ConfigurationSection

class PrimitiveObjectArgument(
    id: String,
    defaultValue: Any?,
    required: Boolean,
    aliases: Collection<String> = listOf(),
) : ObjectArgument<Any?>(
    id, defaultValue,
    required,
    aliases
) {
    override val serializer: ObjectArgumentFactory<Any?>
        get() {
            return Factory
        }

    object Factory : ObjectArgumentFactory<Any?>() {
        override fun load(section: ConfigurationSection, id: String): Any? {
            return section.get(id)
        }
    }
}