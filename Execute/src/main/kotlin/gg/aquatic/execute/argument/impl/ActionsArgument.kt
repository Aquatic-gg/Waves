package gg.aquatic.execute.argument.impl

import gg.aquatic.common.getSectionList
import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.ExecutableObjectHandle
import gg.aquatic.execute.action.ActionSerializer
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.argument.ObjectArgumentFactory
import org.bukkit.configuration.ConfigurationSection

class ActionsArgument<T : Any>(
    id: String, defaultValue: Collection<ExecutableObjectHandle<T, Unit>>?, required: Boolean,
    val clazz: Class<T>,
    val transforms: Collection<ClassTransform<T, *>>, aliases: Collection<String> = listOf(),
) : ObjectArgument<Collection<ExecutableObjectHandle<T, Unit>>>(
    id, defaultValue,
    required, aliases,
) {
    override val serializer: ObjectArgumentFactory<Collection<ExecutableObjectHandle<T, Unit>>?> =
        Factory()

    inner class Factory : ObjectArgumentFactory<Collection<ExecutableObjectHandle<T, Unit>>?>() {
        override fun load(
            section: ConfigurationSection,
            id: String,
        ): Collection<ExecutableObjectHandle<T, Unit>> {
            return ActionSerializer.fromSections(clazz, section.getSectionList(id), *transforms.toTypedArray())
        }
    }
}