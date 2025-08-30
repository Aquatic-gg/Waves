package gg.aquatic.waves.util.argument.impl

import gg.aquatic.waves.registry.serializer.ActionSerializer
import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.generic.ClassTransform
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import gg.aquatic.waves.util.getSectionList
import org.bukkit.configuration.ConfigurationSection

class ActionsArgument<T : Any>(
    id: String, defaultValue: Collection<ConfiguredExecutableObject<T, Unit>>?, required: Boolean,
    val clazz: Class<T>,
    val transforms: Collection<ClassTransform<T, *>>, aliases: Collection<String> = listOf(),
) : AquaticObjectArgument<Collection<ConfiguredExecutableObject<T, Unit>>>(
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