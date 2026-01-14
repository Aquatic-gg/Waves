package gg.aquatic.execute.action

import gg.aquatic.execute.Action
import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.ExecutableObjectHandle
import gg.aquatic.execute.action.impl.logical.ConditionalActionsAction
import gg.aquatic.execute.action.impl.logical.SmartAction
import gg.aquatic.execute.argument.ArgumentContext
import gg.aquatic.execute.argument.ObjectArgument
import org.bukkit.configuration.ConfigurationSection

object ActionSerializer {

    val smartActions = mutableMapOf<String, (
        clazz: Class<*>,
        classTransforms: Collection<ClassTransform<*, *>>,
    ) -> SmartAction<*>>(
        "conditional-actions" to { clazz, classTransforms -> ConditionalActionsAction(clazz, classTransforms) },
    )

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getSmartAction(
        id: String,
        clazz: Class<T>,
        classTransforms: Collection<ClassTransform<T, *>>
    ): SmartAction<T>? {
        val factory = SmartAction.REGISTRY[id] ?: return null
        return factory(clazz, classTransforms) as? SmartAction<T>
    }

    inline fun <reified T : Any> fromSectionSimple(
        section: ConfigurationSection,
    ): ExecutableObjectHandle<T, Unit>? {
        return fromSectionSimple(T::class.java, section)
    }

    fun <T : Any> fromSectionSimple(
        clazz: Class<T>,
        section: ConfigurationSection,
    ): ExecutableObjectHandle<T, Unit>? {
        val type = section.getString("type") ?: return null
        //val action = WavesRegistry.getAction<T>(type) ?: return null

        val smartAction = getSmartAction(type, clazz, emptyList())
        if (smartAction != null) {
            val args = ObjectArgument.load(section, smartAction.arguments)
            return ExecutableObjectHandle(smartAction, args)
        }

        val allActions = Action.REGISTRY.getAllHierarchical(clazz)

        val action = allActions[type]
        if (action == null) {
            if (clazz == Unit::class.java) return null

            val voidActions = Action.REGISTRY.getActions<Unit>()
            val voidAction = voidActions[type] ?: return null
            val action = TransformedAction<T, Unit>(voidAction) { _ -> let { } }

            val args = ObjectArgument.load(section, voidAction.arguments)
            val configuredAction = ExecutableObjectHandle(action as Action<T>, args)
            return configuredAction
        }

        val args = ObjectArgument.load(section, action.arguments)

        val configuredAction = ExecutableObjectHandle(action, args)
        return configuredAction
    }

    inline fun <reified T : Any> fromSection(
        section: ConfigurationSection,
        vararg classTransforms: ClassTransform<T, *>,
    ): ExecutableObjectHandle<T, Unit>? {
        return fromSection(T::class.java, section, *classTransforms)
    }

    fun <T : Any> fromSection(
        clazz: Class<T>,
        section: ConfigurationSection,
        vararg classTransforms: ClassTransform<T, *>,
    ): ExecutableObjectHandle<T, Unit>? {
        val action = fromSectionSimple(clazz, section)
        if (action != null) return action
        val type = section.getString("type") ?: return null

        val smartAction = getSmartAction(type, clazz, emptyList())
        if (smartAction != null) {
            val args = ObjectArgument.load(section, smartAction.arguments)
            return ExecutableObjectHandle(smartAction, args)
        }

        for (transform in classTransforms) {
            val tranformAction = transform.createTransformedAction(type) ?: continue
            val args = ObjectArgument.load(section, tranformAction.arguments)
            val configuredAction = ExecutableObjectHandle(tranformAction, args)
            return configuredAction
        }
        return null
    }

    inline fun <reified T : Any> fromSections(
        sections: List<ConfigurationSection>,
        vararg classTransforms: ClassTransform<T, *>,
    ): List<ExecutableObjectHandle<T, Unit>> {
        return fromSections(T::class.java, sections, *classTransforms)
    }

    fun <T : Any> fromSections(
        clazz: Class<T>,
        sections: List<ConfigurationSection>,
        vararg classTransforms: ClassTransform<T, *>,
    ): List<ExecutableObjectHandle<T, Unit>> {
        return sections.mapNotNull { fromSection(clazz, it, *classTransforms) }
    }

    class TransformedAction<T : Any, D : Any>(val externalAction: Action<D>, val transform: (T) -> D?) : Action<T> {
        override suspend fun execute(
            binder: T,
            args: ArgumentContext<T>,
        ) {
            val transformed = transform(binder) ?: return
            val args = ArgumentContext(transformed, args.arguments) { _, str -> args.updater(binder, str) }
            externalAction.execute(transformed, args)
        }

        override val arguments: List<ObjectArgument<*>> = externalAction.arguments
    }
}