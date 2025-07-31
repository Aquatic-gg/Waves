package gg.aquatic.waves.registry.serializer

import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.registry.getRequirement
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.requirement.ConfiguredRequirement
import org.bukkit.configuration.ConfigurationSection

object RequirementSerializer {

    inline fun <reified T : Any> fromSection(section: ConfigurationSection): ConfiguredRequirement<T>? {
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

    inline fun <reified T: Any> fromSections(sections: List<ConfigurationSection>): List<ConfiguredRequirement<T>> {
        return sections.mapNotNull { fromSection(it) }
    }

}