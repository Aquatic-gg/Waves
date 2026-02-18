package gg.aquatic.execute.arguments

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArgumentFactory
import gg.aquatic.common.getSectionList
import gg.aquatic.execute.ExecutableObjectHandle
import gg.aquatic.execute.condition.ConditionSerializer
import org.bukkit.configuration.ConfigurationSection

class ConditionsArgument<T : Any>(
    id: String, defaultValue: Collection<ExecutableObjectHandle<T, Boolean>>?, required: Boolean,
    val clazz: Class<T>,
    aliases: Collection<String> = listOf(),
) : ObjectArgument<Collection<ExecutableObjectHandle<T, Boolean>>?>(
    id, defaultValue,
    required, aliases,
) {
    override val serializer: ObjectArgumentFactory<Collection<ExecutableObjectHandle<T, Boolean>>?> =
        Factory()


    inner class Factory : ObjectArgumentFactory<Collection<ExecutableObjectHandle<T, Boolean>>?>() {
        override fun load(
            section: ConfigurationSection,
            id: String,
        ): Collection<ExecutableObjectHandle<T, Boolean>> {
            return ConditionSerializer.fromSections(clazz, section.getSectionList(id))
        }
    }
}