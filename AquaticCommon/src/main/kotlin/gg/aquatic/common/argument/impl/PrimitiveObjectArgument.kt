package gg.aquatic.common.argument.impl

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArgumentFactory
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