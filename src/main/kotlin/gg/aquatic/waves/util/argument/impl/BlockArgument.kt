package gg.aquatic.waves.util.argument.impl

import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.block.AquaticBlock
import gg.aquatic.waves.util.block.AquaticBlockSerializer
import org.bukkit.configuration.ConfigurationSection

class BlockArgument(id: String, defaultValue: AquaticBlock?, required: Boolean) : AquaticObjectArgument<AquaticBlock>(id, defaultValue,
    required
) {
    override val serializer: AbstractObjectArgumentSerializer<AquaticBlock?> = Serializer

    override fun load(section: ConfigurationSection): AquaticBlock? {
        return serializer.load(section, id)
    }

    object Serializer : AbstractObjectArgumentSerializer<AquaticBlock?>() {
        override fun load(section: ConfigurationSection, id: String): AquaticBlock? {
            val s = section.getConfigurationSection(id) ?: return null
            return AquaticBlockSerializer.load(s)
        }

    }
}