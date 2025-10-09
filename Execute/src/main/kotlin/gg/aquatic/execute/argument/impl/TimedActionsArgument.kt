package gg.aquatic.execute.argument.impl

import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.action.ActionSerializer
import gg.aquatic.execute.argument.AbstractObjectArgumentSerializer
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.ConfiguredExecutableObject
import gg.aquatic.execute.getSectionList
import org.bukkit.configuration.ConfigurationSection

class TimedActionsArgument<T : Any>(
    id: String, defaultValue: HashMap<Int, Collection<ConfiguredExecutableObject<T, Unit>>>?, required: Boolean,
    val clazz: Class<T>,
    val transforms: Collection<ClassTransform<T, *>>, aliases: Collection<String> = listOf()
) : ObjectArgument<HashMap<Int, Collection<ConfiguredExecutableObject<T, Unit>>>>(
    id, defaultValue,
    required, aliases,
) {
    override val serializer: AbstractObjectArgumentSerializer<HashMap<Int, Collection<ConfiguredExecutableObject<T, Unit>>>?>
        get() = Serializer()

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