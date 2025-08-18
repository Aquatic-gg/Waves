package gg.aquatic.waves.registry.serializer

import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.util.action.impl.logical.ConditionalActionsAction
import gg.aquatic.waves.util.action.impl.logical.RandomActionAction
import gg.aquatic.waves.util.action.impl.logical.SmartAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.generic.ClassTransform
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import org.bukkit.configuration.ConfigurationSection

object ActionSerializer {

    val smartActions = mutableMapOf<String, (
        clazz: Class<*>,
        classTransforms: Collection<ClassTransform<*,*>>,
    ) -> SmartAction<*>>(
        "random-action" to { clazz, classTransforms -> RandomActionAction(clazz, classTransforms) },
        "conditional-actions" to { clazz, classTransforms -> ConditionalActionsAction(clazz, classTransforms) },
    )

    private fun <T: Any> getSmartAction(id: String,clazz: Class<T>, classTransforms: Collection<ClassTransform<T, *>>): SmartAction<T>? {
        val type = smartActions[id] ?: return null
        return type(clazz, classTransforms) as? SmartAction<T>
    }

    inline fun <reified T : Any> fromSectionSimple(
        section: ConfigurationSection,
    ): ConfiguredExecutableObject<T, Unit>? {
        return fromSectionSimple(T::class.java, section)
    }

    fun <T : Any> fromSectionSimple(
        clazz: Class<T>,
        section: ConfigurationSection,
    ): ConfiguredExecutableObject<T, Unit>? {
        val type = section.getString("type") ?: return null
        //val action = WavesRegistry.getAction<T>(type) ?: return null

        val smartAction = getSmartAction(type, clazz, emptyList())
        if (smartAction != null) {
            val args = AquaticObjectArgument.loadRequirementArguments(section, smartAction.arguments)
            return ConfiguredExecutableObject(smartAction, args)
        }

        val actions = allActions(clazz)
        val action = actions[type]
        if (action == null) {
            if (clazz == Unit::class.java) return null
            val voidActions = WavesRegistry.ACTION[Unit::class.java] ?: return null
            val voidAction = voidActions[type] ?: return null
            val action = TransformedAction<T, Unit>(voidAction as Action<Unit>) { d -> let { } }

            val args = AquaticObjectArgument.loadRequirementArguments(section, voidAction.arguments)
            val configuredAction = ConfiguredExecutableObject(action as Action<T>, args)
            return configuredAction
        }

        val args = AquaticObjectArgument.loadRequirementArguments(section, action.arguments)

        val configuredAction = ConfiguredExecutableObject(action as Action<T>, args)
        return configuredAction
    }

    inline fun <reified T : Any> fromSection(
        section: ConfigurationSection,
        vararg classTransforms: ClassTransform<T, *>,
    ): ConfiguredExecutableObject<T, Unit>? {
        return fromSection(T::class.java, section, *classTransforms)
    }

    fun <T : Any> fromSection(
        clazz: Class<T>,
        section: ConfigurationSection,
        vararg classTransforms: ClassTransform<T, *>,
    ): ConfiguredExecutableObject<T, Unit>? {
        val action = fromSectionSimple(clazz, section)
        if (action != null) return action
        val type = section.getString("type") ?: return null

        val smartAction = getSmartAction(type, clazz, emptyList())
        if (smartAction != null) {
            val args = AquaticObjectArgument.loadRequirementArguments(section, smartAction.arguments)
            return ConfiguredExecutableObject(smartAction, args)
        }

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
        return fromSections(T::class.java, sections, *classTransforms)
    }

    fun <T : Any> fromSections(
        clazz: Class<T>,
        sections: List<ConfigurationSection>,
        vararg classTransforms: ClassTransform<T, *>,
    ): List<ConfiguredExecutableObject<T, Unit>> {
        return sections.mapNotNull { fromSection(clazz, it, *classTransforms) }
    }

    class TransformedAction<T : Any, D : Any>(val externalAction: Action<D>, val transform: (T) -> D?) : Action<T> {
        override fun execute(
            binder: T,
            args: ObjectArguments,
            textUpdater: (T, String) -> String,
        ) {
            val transformed = transform(binder) ?: return
            externalAction.execute(transformed, args) { d, str -> textUpdater(binder, str) }
        }

        override val arguments: List<AquaticObjectArgument<*>> = externalAction.arguments
    }

    fun <T : Any> allActions(type: Class<T>): Map<String,Action<T>> {
        val actions = hashMapOf<String,Action<T>>()
        for ((clazz, typeActions) in WavesRegistry.ACTION) {
            if (type == clazz || clazz.isAssignableFrom(type)) {
                actions += typeActions as Map<String,Action<T>>
            }
        }
        return actions
    }

}