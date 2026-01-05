package gg.aquatic.execute.requirement

import gg.aquatic.execute.argument.ArgumentContext
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.Condition
import gg.aquatic.execute.argument.impl.PrimitiveObjectArgument
import org.bukkit.configuration.ConfigurationSection

object ConditionSerializer {

    inline fun <reified T : Any> fromSectionSimple(
        section: ConfigurationSection,
    ): ConditionHandle<T>? {
        return fromSectionSimple(T::class.java, section)
    }

    fun <T : Any> fromSectionSimple(
        clazz: Class<T>,
        section: ConfigurationSection,
    ): ConditionHandle<T>? {
        val type = section.getString("type") ?: return null

        val actions = Condition.REGISTRY.getAllHierarchical<T>()
        val action = actions[type]
        if (action == null) {
            if (clazz == Unit::class.java) return null


            val voidRequirement = Condition.REGISTRY.getHierarchical<Unit>(type) ?: return null
            val condition = TransformedCondition<T, Unit>(voidRequirement) { _ -> let { } }

            val arguments = condition.arguments.toMutableList()
            arguments += PrimitiveObjectArgument("negate", false, required = false)
            val args = ObjectArgument.load(section, arguments)
            val configuredAction = ConditionHandle(condition, args)
            return configuredAction
        }

        val arguments = action.arguments.toMutableList()
        arguments += PrimitiveObjectArgument("negate", false, required = false)
        val args = ObjectArgument.load(section, arguments)

        val configuredAction = ConditionHandle(action, args)
        return configuredAction
    }

    inline fun <reified T : Any> fromSection(
        section: ConfigurationSection,
        vararg classTransforms: ClassTransform<T, *>,
    ): ConditionHandle<T>? {
        return fromSection(T::class.java, section, *classTransforms)
    }

    fun <T : Any> fromSection(
        clazz: Class<T>,
        section: ConfigurationSection,
        vararg classTransforms: ClassTransform<T, *>,
    ): ConditionHandle<T>? {
        val action = fromSectionSimple(clazz, section)
        if (action != null) return action
        val type = section.getString("type") ?: return null

        for (transform in classTransforms) {
            val tranformAction = transform.createTransformedCondition(type) ?: continue
            val arguments = tranformAction.arguments.toMutableList()
            arguments += PrimitiveObjectArgument("negate", false, required = false)
            val args = ObjectArgument.load(section, arguments)
            val configuredAction = ConditionHandle(tranformAction, args)
            return configuredAction
        }
        return null
    }

    inline fun <reified T : Any> fromSections(
        sections: List<ConfigurationSection>,
        vararg classTransforms: ClassTransform<T, *>,
    ): List<ConditionHandle<T>> {
        return fromSections(T::class.java, sections, *classTransforms)
    }

    fun <T : Any> fromSections(
        clazz: Class<T>,
        sections: List<ConfigurationSection>,
        vararg classTransforms: ClassTransform<T, *>,
    ): List<ConditionHandle<T>> {
        return sections.mapNotNull { fromSection(clazz, it, *classTransforms) }
    }

    class TransformedCondition<T : Any, D : Any>(val externalAction: Condition<D>, val transform: (T) -> D?) :
        Condition<T> {

        override suspend fun execute(binder: T, args: ArgumentContext<T>): Boolean {
            val transformed = transform(binder) ?: return false
            val args = ArgumentContext(transformed, args.arguments) { _, str -> args.updater(binder, str) }
            return externalAction.execute(transformed, args)
        }

        override val arguments: List<ObjectArgument<*>> = externalAction.arguments
    }
}