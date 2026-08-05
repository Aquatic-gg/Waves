package gg.aquatic.blokk.argument

import gg.aquatic.blokk.Blokk
import gg.aquatic.blokk.BlokkSerializer
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArgumentFactory
import org.bukkit.configuration.ConfigurationSection

class BlockArgument(
    id: String,
    defaultValue: Blokk?,
    required: Boolean,
    aliases: Collection<String> = listOf(),
) : ObjectArgument<Blokk>(
    id, defaultValue,
    required,
    aliases
) {
    override val serializer: ObjectArgumentFactory<Blokk?> = Serializer

    object Serializer : ObjectArgumentFactory<Blokk?>() {
        override fun load(section: ConfigurationSection, id: String): Blokk? {
            val s = section.getConfigurationSection(id) ?: return null
            return BlokkSerializer.load(s)
        }

    }
}