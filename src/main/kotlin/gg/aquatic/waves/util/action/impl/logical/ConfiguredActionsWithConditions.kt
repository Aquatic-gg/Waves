package gg.aquatic.waves.util.action.impl.logical

import gg.aquatic.waves.registry.serializer.ActionSerializer
import gg.aquatic.waves.registry.serializer.RequirementSerializer
import gg.aquatic.waves.util.generic.ClassTransform
import gg.aquatic.waves.util.generic.ConfiguredExecutableObjectWithConditions
import gg.aquatic.waves.util.generic.ConfiguredExecutableObjectsWithConditions
import gg.aquatic.waves.util.getSectionList
import gg.aquatic.waves.util.requirement.ConfiguredRequirementWithFailActions
import org.bukkit.configuration.ConfigurationSection
import kotlin.collections.plusAssign

class ConfiguredActionsWithConditions<T>(
    executableObjects: Collection<ConfiguredExecutableObjectWithConditions<T, Unit>>,
    conditions: Collection<ConfiguredRequirementWithFailActions<T, Unit>>,
    failExecutableObjects: ConfiguredExecutableObjectsWithConditions<T>?,
) : ConfiguredExecutableObjectsWithConditions<T>(
    executableObjects, conditions, failExecutableObjects
) {

    companion object {
        inline fun <reified T : Any> fromSection(
            section: ConfigurationSection,
            vararg classTransforms: ClassTransform<T, *>,
        ): ConfiguredActionsWithConditions<T>? {
            return fromSection(T::class.java, section, *classTransforms)
        }

        fun <T : Any> fromSection(
            clazz: Class<T>,
            section: ConfigurationSection,
            vararg classTransforms: ClassTransform<T, *>,
        ): ConfiguredActionsWithConditions<T>? {
            val actions = ArrayList<ConfiguredExecutableObjectWithConditions<T, Unit>>()
            val actionSections = section.getSectionList("actions")

            for (actionSection in actionSections) {
                actions += loadActionWithCondition(clazz, actionSection, *classTransforms) ?: continue
            }
            val conditions = ArrayList<ConfiguredRequirementWithFailActions<T, Unit>>()
            for (conditionSection in section.getSectionList("conditions")) {
                conditions += loadConditionWithFailActions(clazz, conditionSection, *classTransforms) ?: continue
            }

            if (actions.isEmpty() && conditions.isEmpty()) return null

            val failActions = if (section.isConfigurationSection("fail") && conditions.isNotEmpty()) {
                fromSection(clazz, section.getConfigurationSection("fail")!!, *classTransforms)
            } else null

            return ConfiguredActionsWithConditions(actions, conditions, failActions)
        }

        fun <T : Any> loadActionWithCondition(
            clazz: Class<T>,
            section: ConfigurationSection,
            vararg classTransforms: ClassTransform<T, *>,
        ): ConfiguredExecutableObjectWithConditions<T, Unit>? {
            val action = ActionSerializer.fromSection(clazz, section, *classTransforms) ?: return null
            val conditions = ArrayList<ConfiguredRequirementWithFailActions<T, Unit>>()
            for (configurationSection in section.getSectionList("conditions")) {
                conditions += loadConditionWithFailActions(clazz, configurationSection, *classTransforms) ?: continue
            }
            val failActions = if (section.isConfigurationSection("fail") && conditions.isNotEmpty()) {
                fromSection(clazz, section.getConfigurationSection("fail")!!, *classTransforms)
            } else null
            return ConfiguredExecutableObjectWithConditions(action, conditions, failActions)
        }

        fun <T : Any> loadConditionWithFailActions(
            clazz: Class<T>,
            section: ConfigurationSection,
            vararg classTransforms: ClassTransform<T, *>,
        ): ConfiguredRequirementWithFailActions<T, Unit>? {
            val condition = RequirementSerializer.fromSection(clazz, section, *classTransforms) ?: return null
            val failActions = if (section.isConfigurationSection("fail")) {
                fromSection(clazz, section.getConfigurationSection("fail")!!, *classTransforms)
            } else null
            return ConfiguredRequirementWithFailActions(condition, failActions)
        }
    }
}