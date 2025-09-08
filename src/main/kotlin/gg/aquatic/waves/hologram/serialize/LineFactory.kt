package gg.aquatic.waves.hologram.serialize

import gg.aquatic.waves.hologram.CommonHologramLineSettings
import org.bukkit.configuration.ConfigurationSection

interface LineFactory {

    fun load(section: ConfigurationSection, commonOptions: CommonHologramLineSettings): LineSettings?

}