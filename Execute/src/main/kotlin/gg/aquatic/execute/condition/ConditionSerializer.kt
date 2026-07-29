package gg.aquatic.execute.condition

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.Condition
import gg.aquatic.execute.Condition.Companion.getHierarchical
import org.bukkit.configuration.ConfigurationSection

object ConditionSerializer {

    inline fun <reified T : Any> fromSection(
        section: ConfigurationSection,
    ): ConditionHandle<T>? {
        return fromSection(T::class.java, section)
    }

    fun <T : Any> fromSection(
        clazz: Class<T>,
        section: ConfigurationSection,
    ): ConditionHandle<T>? {
        val type = section.getString("type") ?: return null
        val condition = Condition.REGISTRY.getHierarchical(type, clazz) ?: return null

        val arguments = condition.arguments.toMutableList()
        arguments += PrimitiveObjectArgument("negate", false, required = false)
        val args = ObjectArgument.load(section, arguments)

        val configuredAction = ConditionHandle(condition, args)
        return configuredAction
    }

    inline fun <reified T : Any> fromSections(
        sections: List<ConfigurationSection>
    ): List<ConditionHandle<T>> {
        return fromSections(T::class.java, sections)
    }

    fun <T : Any> fromSections(
        clazz: Class<T>,
        sections: List<ConfigurationSection>,
    ): List<ConditionHandle<T>> {
        return sections.mapNotNull { fromSection(clazz, it) }
    }
}