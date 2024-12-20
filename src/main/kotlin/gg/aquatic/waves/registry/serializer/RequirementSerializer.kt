package gg.aquatic.waves.registry.serializer

import gg.aquatic.aquaticseries.lib.requirement.ConfiguredRequirement
import gg.aquatic.aquaticseries.lib.util.argument.AquaticObjectArgument
import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.registry.getRequirement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.configuration.ConfigurationSection

object RequirementSerializer {

    inline fun <reified T : Any> fromSection(section: ConfigurationSection): ConfiguredRequirement<T>? {
        val type = section.getString("type") ?: return null
        val requirement = WavesRegistry.getRequirement<T>(type)
        if (requirement == null) {
            println("[AquaticSeriesLib] Action type $type does not exist!")
            return null
        }

        val arguments = requirement.arguments()
        val args = AquaticObjectArgument.loadRequirementArguments(section, arguments)

        val configuredRequirement = ConfiguredRequirement(requirement, args)
        return configuredRequirement
    }

    inline fun <reified T: Any> fromSections(sections: List<ConfigurationSection>): List<ConfiguredRequirement<T>> {
        return sections.mapNotNull { fromSection(it) }
    }

}