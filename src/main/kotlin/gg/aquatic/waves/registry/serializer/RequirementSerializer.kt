package gg.aquatic.waves.registry.serializer

import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.generic.Condition
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import gg.aquatic.waves.util.requirement.ConfiguredRequirement
import org.bukkit.configuration.ConfigurationSection

object RequirementSerializer {

    /*
    inline fun <reified T : Any> fromSectionSimple(section: ConfigurationSection): ConfiguredRequirement<T>? {
        val type = section.getString("type") ?: return null
        val requirement = WavesRegistry.getRequirement<T>(type)
        if (requirement == null) {
            println("[Waves] Requirement type $type does not exist!")
            return null
        }

        val args = AquaticObjectArgument.loadRequirementArguments(section, requirement.arguments)

        val configuredRequirement = ConfiguredRequirement(requirement, args)
        return configuredRequirement
    }
     */

    inline fun <reified T : Any> fromSectionSimple(
        section: ConfigurationSection,
    ): ConfiguredRequirement<T>? {
        val type = section.getString("type") ?: return null
        //val action = WavesRegistry.getAction<T>(type) ?: return null

        val actions = WavesRegistry.REQUIREMENT[T::class.java] ?: HashMap()
        for (klass in WavesRegistry.REQUIREMENT.keys) {
            if (klass == T::class.java) continue
            if (klass.isAssignableFrom(T::class.java)) {
                actions += WavesRegistry.REQUIREMENT[klass] ?: HashMap()
            }
        }

        val action = actions[type]
        if (action == null) {
            if (T::class.java == Unit::class.java) return null
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

    /*
    inline fun <reified T: Any> fromSections(sections: List<ConfigurationSection>): List<ConfiguredRequirement<T>> {
        return sections.mapNotNull { fromSection(it) }
    }

     */

    inline fun <reified T : Any> fromSection(
        section: ConfigurationSection,
        vararg classTransforms: ClassTransform<T, *>,
    ): ConfiguredRequirement<T>? {
        val action = fromSectionSimple<T>(section)
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
        return sections.mapNotNull { fromSection(it, *classTransforms) }
    }

    class ClassTransform<T : Any, D : Any>(val clazz: Class<T>, val transform: (T) -> D) {
        fun transform(obj: T): D {
            return transform(obj)
        }

        val registeredRequirements: MutableMap<String, Condition<*>>
            get() {
                val requirements = WavesRegistry.REQUIREMENT[clazz] ?: HashMap()
                for (klass in WavesRegistry.REQUIREMENT.keys) {
                    if (klass == clazz) continue
                    if (klass.isAssignableFrom(clazz)) {
                        requirements += WavesRegistry.REQUIREMENT[klass] ?: HashMap()
                    }
                }
                return requirements
            }

        fun createTransformedRequirement(id: String): TransformedRequirement<T, D>? {
            val requirement = registeredRequirements[id]
            if (requirement == null) {
                if (clazz == Unit::class.java) return null
                val voidRequirements = WavesRegistry.REQUIREMENT[Unit::class.java] ?: return null
                val voidRequirement = voidRequirements[id] ?: return null
                return TransformedRequirement(
                    TransformedRequirement(voidRequirement as Condition<Unit>) { d -> let { } },
                    transform
                )
            }
            return TransformedRequirement(requirement as Condition<D>, transform)
        }
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