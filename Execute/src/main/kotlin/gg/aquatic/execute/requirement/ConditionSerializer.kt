package gg.aquatic.execute.requirement

import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.argument.ObjectArguments
import gg.aquatic.execute.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.Condition
import gg.aquatic.execute.argument.ArgumentContext
import org.bukkit.configuration.ConfigurationSection

object ConditionSerializer {

    inline fun <reified T : Any> fromSectionSimple(
        section: ConfigurationSection,
    ): ConfiguredCondition<T>? {
        return fromSectionSimple(T::class.java, section)
    }

    fun <T : Any> fromSectionSimple(
        clazz: Class<T>,
        section: ConfigurationSection,
    ): ConfiguredCondition<T>? {
        val type = section.getString("type") ?: return null

        val actions = allRequirements(clazz)
        val action = actions[type]
        if (action == null) {
            if (clazz == Unit::class.java) return null
            val voidRequirements = CONDITIONS[Unit::class.java] ?: return null
            val voidRequirement = voidRequirements[type] ?: return null
            val requirement = TransformedRequirement<T, Unit>(voidRequirement as Condition<Unit>) { d -> let { } }

            val arguments = requirement.arguments.toMutableList()
            arguments += PrimitiveObjectArgument("negate", false, required = false)
            val args = ObjectArgument.Companion.loadRequirementArguments(section, arguments)
            val configuredAction = ConfiguredCondition(requirement as Condition<T>, args)
            return configuredAction
        }

        val arguments = action.arguments.toMutableList()
        arguments += PrimitiveObjectArgument("negate", false, required = false)
        val args = ObjectArgument.Companion.loadRequirementArguments(section, arguments)

        val configuredAction = ConfiguredCondition(action as Condition<T>, args)
        return configuredAction
    }

    inline fun <reified T : Any> fromSection(
        section: ConfigurationSection,
        vararg classTransforms: ClassTransform<T, *>,
    ): ConfiguredCondition<T>? {
        return fromSection(T::class.java, section, *classTransforms)
    }

    fun <T : Any> fromSection(
        clazz: Class<T>,
        section: ConfigurationSection,
        vararg classTransforms: ClassTransform<T, *>,
    ): ConfiguredCondition<T>? {
        val action = fromSectionSimple(clazz, section)
        if (action != null) return action
        val type = section.getString("type") ?: return null

        for (transform in classTransforms) {
            val tranformAction = transform.createTransformedRequirement(type) ?: continue
            val arguments = tranformAction.arguments.toMutableList()
            arguments += PrimitiveObjectArgument("negate", false, required = false)
            val args = ObjectArgument.Companion.loadRequirementArguments(section, arguments)
            val configuredAction = ConfiguredCondition(tranformAction, args)
            return configuredAction
        }
        return null
    }

    inline fun <reified T : Any> fromSections(
        sections: List<ConfigurationSection>,
        vararg classTransforms: ClassTransform<T, *>,
    ): List<ConfiguredCondition<T>> {
        return fromSections(T::class.java, sections, *classTransforms)
    }

    fun <T : Any> fromSections(
        clazz: Class<T>,
        sections: List<ConfigurationSection>,
        vararg classTransforms: ClassTransform<T, *>,
    ): List<ConfiguredCondition<T>> {
        return sections.mapNotNull { fromSection(clazz, it, *classTransforms) }
    }

    class TransformedRequirement<T : Any, D : Any>(val externalAction: Condition<D>, val transform: (T) -> D?) :
        Condition<T> {
        override fun execute(binder: T, args: ArgumentContext<T>): Boolean {
            val transformed = transform(binder) ?: return false
            val args = ArgumentContext(transformed, args.arguments, { d, str -> args.updater(binder, str) })
            return externalAction.execute(transformed, args)
        }

        override val arguments: List<ObjectArgument<*>> = externalAction.arguments
    }

    fun <T : Any> allRequirements(type: Class<T>): Map<String, Condition<T>> {
        val actions = hashMapOf<String, Condition<T>>()
        for ((clazz, typeActions) in CONDITIONS) {
            if (type == clazz || clazz.isAssignableFrom(type)) {
                actions += typeActions as Map<String, Condition<T>>
            }
        }
        return actions
    }
}