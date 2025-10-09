package gg.aquatic.execute.argument.impl

import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.ExecutableObjectHandle
import gg.aquatic.execute.argument.ArgumentFactory
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.requirement.ConditionSerializer
import gg.aquatic.execute.getSectionList
import org.bukkit.configuration.ConfigurationSection

class ConditionsArgument<T : Any>(
    id: String, defaultValue: Collection<ExecutableObjectHandle<T, Boolean>>?, required: Boolean,
    val clazz: Class<T>,
    val transforms: Collection<ClassTransform<T, *>>, aliases: Collection<String> = listOf(),
) : ObjectArgument<Collection<ExecutableObjectHandle<T, Boolean>>?>(
    id, defaultValue,
    required, aliases,
) {
    override val serializer: ArgumentFactory<Collection<ExecutableObjectHandle<T, Boolean>>?> =
        Serializer()


    inner class Serializer() : ArgumentFactory<Collection<ExecutableObjectHandle<T, Boolean>>?>() {
        override fun load(
            section: ConfigurationSection,
            id: String,
        ): Collection<ExecutableObjectHandle<T, Boolean>>? {
            return ConditionSerializer.fromSections(clazz, section.getSectionList(id), *transforms.toTypedArray())
        }
    }
}