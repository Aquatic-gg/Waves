package gg.aquatic.stacked.argument

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArgumentFactory
import gg.aquatic.stacked.impl.StackedItemImpl
import org.bukkit.configuration.ConfigurationSection

class ItemObjectArgument(
    id: String,
    defaultValue: StackedItemImpl?,
    required: Boolean,
    aliases: Collection<String> = listOf(),
) : ObjectArgument<StackedItemImpl>(
    id, defaultValue,
    required,
    aliases
) {
    override val serializer: ObjectArgumentFactory<StackedItemImpl?>
        get() {
            return Serializer
        }

    object Serializer : ObjectArgumentFactory<StackedItemImpl?>() {
        override fun load(section: ConfigurationSection, id: String): StackedItemImpl? {
            return StackedItemImpl.loadFromYml(section.getConfigurationSection(id) ?: return null)
        }
    }
}