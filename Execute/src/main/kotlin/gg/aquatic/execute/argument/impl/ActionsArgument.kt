package gg.aquatic.execute.argument.impl

import gg.aquatic.execute.action.ActionSerializer
import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.ConfiguredExecutableObject
import gg.aquatic.execute.argument.AbstractObjectArgumentSerializer
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.getSectionList
import org.bukkit.configuration.ConfigurationSection

class ActionsArgument<T : Any>(
    id: String, defaultValue: Collection<ConfiguredExecutableObject<T, Unit>>?, required: Boolean,
    val clazz: Class<T>,
    val transforms: Collection<ClassTransform<T, *>>, aliases: Collection<String> = listOf(),
) : ObjectArgument<Collection<ConfiguredExecutableObject<T, Unit>>>(
    id, defaultValue,
    required, aliases,
) {
    override val serializer: AbstractObjectArgumentSerializer<Collection<ConfiguredExecutableObject<T, Unit>>?> =
        Serializer()

    inner class Serializer() : AbstractObjectArgumentSerializer<Collection<ConfiguredExecutableObject<T, Unit>>?>() {
        override fun load(
            section: ConfigurationSection,
            id: String,
        ): Collection<ConfiguredExecutableObject<T, Unit>> {
            return ActionSerializer.fromSections(clazz, section.getSectionList(id), *transforms.toTypedArray())
        }
    }
}