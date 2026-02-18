package gg.aquatic.execute.action

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.execute.Action
import gg.aquatic.execute.Action.Companion.getHierarchical
import gg.aquatic.execute.ExecutableObjectHandle
import gg.aquatic.execute.action.impl.logical.SmartAction
import org.bukkit.configuration.ConfigurationSection

object ActionSerializer {

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getSmartAction(
        id: String,
        clazz: Class<T>
    ): SmartAction<T>? {
        val factory = SmartAction.REGISTRY[id] ?: return null
        return factory(clazz) as? SmartAction<T>
    }

    fun <T : Any> fromSection(
        clazz: Class<T>,
        section: ConfigurationSection,
    ): ExecutableObjectHandle<T, Unit>? {
        val type = section.getString("type") ?: return null

        val smartAction = getSmartAction(type, clazz)
        if (smartAction != null) {
            val args = ObjectArgument.load(section, smartAction.arguments)
            return ExecutableObjectHandle(smartAction, args)
        }

        val action = Action.REGISTRY.getHierarchical(type, clazz) ?: return null
        val args = ObjectArgument.load(section, action.arguments)
        val configuredAction = ExecutableObjectHandle(action, args)
        return configuredAction
    }

    inline fun <reified T : Any> fromSection(
        section: ConfigurationSection,
    ): ExecutableObjectHandle<T, Unit>? {
        return fromSection(T::class.java, section)
    }

    inline fun <reified T : Any> fromSections(
        sections: List<ConfigurationSection>,
    ): List<ExecutableObjectHandle<T, Unit>> {
        return fromSections(T::class.java, sections)
    }

    fun <T : Any> fromSections(
        clazz: Class<T>,
        sections: List<ConfigurationSection>,
    ): List<ExecutableObjectHandle<T, Unit>> {
        return sections.mapNotNull { fromSection(clazz, it) }
    }
}