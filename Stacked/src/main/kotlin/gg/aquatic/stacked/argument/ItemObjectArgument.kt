package gg.aquatic.stacked.argument

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArgumentFactory
import gg.aquatic.stacked.StackedItem
import org.bukkit.configuration.ConfigurationSection

class ItemObjectArgument(
    id: String,
    defaultValue: StackedItem?,
    required: Boolean,
    aliases: Collection<String> = listOf(),
) : ObjectArgument<StackedItem>(
    id, defaultValue,
    required,
    aliases
) {
    override val serializer: ObjectArgumentFactory<StackedItem?>
        get() {
            return Serializer
        }

    object Serializer : ObjectArgumentFactory<StackedItem?>() {
        override fun load(section: ConfigurationSection, id: String): StackedItem? {
            return StackedItem.Companion.loadFromYml(section.getConfigurationSection(id) ?: return null)
        }
    }
}