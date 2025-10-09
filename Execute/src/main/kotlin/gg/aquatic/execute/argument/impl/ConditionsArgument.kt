package gg.aquatic.execute.argument.impl

import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.ConfiguredExecutableObject
import gg.aquatic.execute.argument.AbstractObjectArgumentSerializer
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.requirement.ConditionSerializer
import gg.aquatic.execute.getSectionList
import org.bukkit.configuration.ConfigurationSection

class ConditionsArgument<T : Any>(
    id: String, defaultValue: Collection<ConfiguredExecutableObject<T, Boolean>>?, required: Boolean,
    val clazz: Class<T>,
    val transforms: Collection<ClassTransform<T, *>>, aliases: Collection<String> = listOf(),
) : ObjectArgument<Collection<ConfiguredExecutableObject<T, Boolean>>?>(
    id, defaultValue,
    required, aliases,
) {
    override val serializer: AbstractObjectArgumentSerializer<Collection<ConfiguredExecutableObject<T, Boolean>>?> =
        Serializer()


    inner class Serializer() : AbstractObjectArgumentSerializer<Collection<ConfiguredExecutableObject<T, Boolean>>?>() {
        override fun load(
            section: ConfigurationSection,
            id: String,
        ): Collection<ConfiguredExecutableObject<T, Boolean>>? {
            return ConditionSerializer.fromSections(clazz, section.getSectionList(id), *transforms.toTypedArray())
        }
    }
}