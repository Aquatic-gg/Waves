package gg.aquatic.waves.registry.serializer

import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.registry.getAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import org.bukkit.configuration.ConfigurationSection

object ActionSerializer {

    inline fun <reified T : Any> fromSection(
        section: ConfigurationSection,
    ): ConfiguredExecutableObject<T, Unit>? {
        val type = section.getString("type") ?: return null
        //val action = WavesRegistry.getAction<T>(type) ?: return null

        val actions = WavesRegistry.ACTION[T::class.java] ?: HashMap()
        for (klass in WavesRegistry.ACTION.keys) {
            if (klass == T::class.java) continue
            if (klass.isAssignableFrom(T::class.java)) {
                actions += WavesRegistry.ACTION[klass] ?: HashMap()
            }
        }

        val action = actions[type]
        if (action == null) {
            val voidAction = WavesRegistry.ACTION[Unit::class.java] ?: return null
            val action = TransformedAction<T, Unit>(voidAction as Action<Unit>) { d -> {} }

            val args = AquaticObjectArgument.loadRequirementArguments(section, voidAction.arguments)
            val configuredAction = ConfiguredExecutableObject(action as Action<T>, args)
            return configuredAction
        }

        val args = AquaticObjectArgument.loadRequirementArguments(section, action.arguments)

        val configuredAction = ConfiguredExecutableObject(action as Action<T>, args)
        return configuredAction
    }

    inline fun <reified T : Any> fromSections(sections: List<ConfigurationSection>): List<ConfiguredExecutableObject<T, Unit>> {
        return sections.mapNotNull { fromSection(it) }
    }

    inline fun <reified T : Any> fromSection(
        section: ConfigurationSection,
        vararg classTransforms: ClassTransform<T, *>,
    ): ConfiguredExecutableObject<T, Unit>? {
        val action = fromSection<T>(section)
        if (action != null) return action
        val type = section.getString("type") ?: return null

        for (transform in classTransforms) {
            val tranformAction = transform.createTransformedAction(type) ?: continue
            val args = AquaticObjectArgument.loadRequirementArguments(section, tranformAction.arguments)
            val configuredAction = ConfiguredExecutableObject(tranformAction, args)
            return configuredAction
        }
        return null
    }

    inline fun <reified T : Any> fromSections(
        sections: List<ConfigurationSection>,
        vararg classTransforms: ClassTransform<T, *>,
    ): List<ConfiguredExecutableObject<T, Unit>> {
        return sections.mapNotNull { fromSection(it, *classTransforms) }
    }

    class ClassTransform<T : Any, D : Any>(val clazz: Class<T>, val transform: (T) -> D) {
        fun transform(obj: T): D {
            return transform(obj)
        }

        val registeredActions: MutableMap<String, Action<*>>
            get() {
                val actions = WavesRegistry.ACTION[clazz] ?: HashMap()
                for (klass in WavesRegistry.ACTION.keys) {
                    if (klass == clazz) continue
                    if (klass.isAssignableFrom(clazz)) {
                        actions += WavesRegistry.ACTION[klass] ?: HashMap()
                    }
                }
                return actions
            }

        fun createTransformedAction(id: String): TransformedAction<T, D>? {
            val action = registeredActions[id]
            if (action == null) {
                val voidAction = WavesRegistry.ACTION[Unit::class.java] ?: return null
                return TransformedAction(TransformedAction(voidAction as Action<Unit>) { d -> {} }, transform)
            }
            return TransformedAction(action as Action<D>, transform)
        }
    }

    class TransformedAction<T : Any, D : Any>(val externalAction: Action<D>, val transform: (T) -> D) : Action<T> {
        override fun execute(
            binder: T,
            args: ObjectArguments,
            textUpdater: (T, String) -> String,
        ) {
            val transformed = transform(binder)
            externalAction.execute(transformed, args) { d, str -> textUpdater(binder, str) }
        }

        override val arguments: List<AquaticObjectArgument<*>> = externalAction.arguments
    }

}