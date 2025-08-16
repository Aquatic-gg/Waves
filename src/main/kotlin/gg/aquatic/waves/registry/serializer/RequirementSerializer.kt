package gg.aquatic.waves.registry.serializer

import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.generic.ClassTransform
import gg.aquatic.waves.util.generic.Condition
import gg.aquatic.waves.util.requirement.ConfiguredRequirement
import org.bukkit.configuration.ConfigurationSection

object RequirementSerializer {

    inline fun <reified T : Any> fromSectionSimple(
        section: ConfigurationSection,
    ): ConfiguredRequirement<T>? {
        return fromSectionSimple(T::class.java, section)
    }

    fun <T : Any> fromSectionSimple(
        clazz: Class<T>,
        section: ConfigurationSection,
    ): ConfiguredRequirement<T>? {
        val type = section.getString("type") ?: return null

        val actions = WavesRegistry.REQUIREMENT[clazz] ?: HashMap()
        for (klass in WavesRegistry.REQUIREMENT.keys) {
            if (klass == clazz) continue
            if (klass.isAssignableFrom(clazz)) {
                actions += WavesRegistry.REQUIREMENT[klass] ?: HashMap()
            }
        }

        val action = actions[type]
        if (action == null) {
            if (clazz == Unit::class.java) return null
            val voidRequirements = WavesRegistry.REQUIREMENT[Unit::class.java] ?: return null
            val voidRequirement = voidRequirements[type] ?: return null
            val requirement = TransformedRequirement<T, Unit>(voidRequirement as Condition<Unit>) { d -> let { } }

            val args = AquaticObjectArgument.loadRequirementArguments(section, voidRequirement.arguments)
            val configuredAction = ConfiguredRequirement(requirement as Condition<T>, args)
            return configuredAction
        }

        val args = AquaticObjectArgument.loadRequirementArguments(section, action.arguments)

        val configuredAction = ConfiguredRequirement(action as Condition<T>, args)
        return configuredAction
    }

    inline fun <reified T : Any> fromSection(
        section: ConfigurationSection,
        vararg classTransforms: ClassTransform<T, *>,
    ): ConfiguredRequirement<T>? {
        return fromSection(T::class.java, section, *classTransforms)
    }

    fun <T : Any> fromSection(
        clazz: Class<T>,
        section: ConfigurationSection,
        vararg classTransforms: ClassTransform<T, *>,
    ): ConfiguredRequirement<T>? {
        val action = fromSectionSimple(clazz,section)
        if (action != null) return action
        val type = section.getString("type") ?: return null

        for (transform in classTransforms) {
            val tranformAction = transform.createTransformedRequirement(type) ?: continue
            val args = AquaticObjectArgument.loadRequirementArguments(section, tranformAction.arguments)
            val configuredAction = ConfiguredRequirement(tranformAction, args)
            return configuredAction
        }
        return null
    }

    inline fun <reified T : Any> fromSections(
        sections: List<ConfigurationSection>,
        vararg classTransforms: ClassTransform<T, *>,
    ): List<ConfiguredRequirement<T>> {
        return fromSections(T::class.java, sections, *classTransforms)
    }

    fun <T : Any> fromSections(
        clazz: Class<T>,
        sections: List<ConfigurationSection>,
        vararg classTransforms: ClassTransform<T, *>,
    ): List<ConfiguredRequirement<T>> {
        return sections.mapNotNull { fromSection(clazz, it, *classTransforms) }
    }

    class TransformedRequirement<T : Any, D : Any>(val externalAction: Condition<D>, val transform: (T) -> D?) :
        Condition<T> {
        override fun execute(
            binder: T,
            args: ObjectArguments,
            textUpdater: (T, String) -> String,
        ): Boolean {
            val transformed = transform(binder) ?: return false
            return externalAction.execute(transformed, args) { d, str -> textUpdater(binder, str) }
        }

        override val arguments: List<AquaticObjectArgument<*>> = externalAction.arguments
    }

}