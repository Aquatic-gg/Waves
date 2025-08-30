package gg.aquatic.waves.util.argument.impl

import gg.aquatic.waves.registry.serializer.ActionSerializer
import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.generic.ClassTransform
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import gg.aquatic.waves.util.getSectionList
import org.bukkit.configuration.ConfigurationSection

class TimedActionsArgument<T : Any>(
    id: String, defaultValue: HashMap<Int, Collection<ConfiguredExecutableObject<T, Unit>>>?, required: Boolean,
    val clazz: Class<T>,
    val transforms: Collection<ClassTransform<T,*>>
) : AquaticObjectArgument<HashMap<Int, Collection<ConfiguredExecutableObject<T, Unit>>>>(
    id, defaultValue,
    required,
) {
    override val serializer: AbstractObjectArgumentSerializer<HashMap<Int, Collection<ConfiguredExecutableObject<T, Unit>>>?>
        get() = Serializer()

    override fun load(section: ConfigurationSection): HashMap<Int, Collection<ConfiguredExecutableObject<T, Unit>>>? {
        return serializer.load(section, id)
    }

    inner class Serializer : AbstractObjectArgumentSerializer<HashMap<Int, Collection<ConfiguredExecutableObject<T, Unit>>>?>() {
        override fun load(
            section: ConfigurationSection,
            id: String
        ): HashMap<Int, Collection<ConfiguredExecutableObject<T, Unit>>> {
            val map = hashMapOf<Int, Collection<ConfiguredExecutableObject<T, Unit>>>()
            val actionsSection = section.getConfigurationSection(id) ?: return map
            for (key in actionsSection.getKeys(false)) {
                val sections = actionsSection.getSectionList(key)
                val actions = ActionSerializer.fromSections(clazz, sections, *transforms.toTypedArray())
                map[key.toInt()] = actions
            }
            return map
        }

    }
}