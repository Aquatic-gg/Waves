package gg.aquatic.execute.arguments

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArgumentFactory
import gg.aquatic.common.getSectionList
import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.ExecutableObjectHandle
import gg.aquatic.execute.action.ActionSerializer
import org.bukkit.configuration.ConfigurationSection

class TimedActionsArgument<T : Any>(
    id: String, defaultValue: HashMap<Int, Collection<ExecutableObjectHandle<T, Unit>>>?, required: Boolean,
    val clazz: Class<T>,
    val transforms: Collection<ClassTransform<T, *>>, aliases: Collection<String> = listOf()
) : ObjectArgument<HashMap<Int, Collection<ExecutableObjectHandle<T, Unit>>>>(
    id, defaultValue,
    required, aliases,
) {
    override val serializer: ObjectArgumentFactory<HashMap<Int, Collection<ExecutableObjectHandle<T, Unit>>>?> =
        Factory()

    inner class Factory : ObjectArgumentFactory<HashMap<Int, Collection<ExecutableObjectHandle<T, Unit>>>?>() {
        override fun load(
            section: ConfigurationSection,
            id: String
        ): HashMap<Int, Collection<ExecutableObjectHandle<T, Unit>>> {
            val map = hashMapOf<Int, Collection<ExecutableObjectHandle<T, Unit>>>()
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