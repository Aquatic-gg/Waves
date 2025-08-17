package gg.aquatic.waves.util.argument.impl

import gg.aquatic.waves.registry.serializer.RequirementSerializer
import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.generic.ClassTransform
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import gg.aquatic.waves.util.getSectionList
import org.bukkit.configuration.ConfigurationSection

class ConditionsArgument<T : Any>(
    id: String, defaultValue: Collection<ConfiguredExecutableObject<T, Boolean>>?, required: Boolean,
    val clazz: Class<T>,
    val transforms: Collection<ClassTransform<T,*>>
) : AquaticObjectArgument<Collection<ConfiguredExecutableObject<T, Boolean>>?>(
    id, defaultValue,
    required,
) {
    override val serializer: AbstractObjectArgumentSerializer<Collection<ConfiguredExecutableObject<T, Boolean>>?> = Serializer()

    override fun load(section: ConfigurationSection): Collection<ConfiguredExecutableObject<T, Boolean>>? {
        return serializer.load(section, id)
    }

    inner class Serializer() : AbstractObjectArgumentSerializer<Collection<ConfiguredExecutableObject<T, Boolean>>?>() {
        override fun load(
            section: ConfigurationSection,
            id: String,
        ): Collection<ConfiguredExecutableObject<T, Boolean>>? {
            return RequirementSerializer.fromSections(clazz, section.getSectionList(id), *transforms.toTypedArray())
        }
    }
}