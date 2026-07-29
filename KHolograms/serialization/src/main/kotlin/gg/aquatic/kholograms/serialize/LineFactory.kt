package gg.aquatic.kholograms.serialize

import gg.aquatic.kholograms.CommonHologramLineSettings
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey
import org.bukkit.configuration.ConfigurationSection

interface LineFactory {

    fun load(section: ConfigurationSection, commonOptions: CommonHologramLineSettings): LineSettings?

    companion object {
        val REGISTRY = mutableMapOf<String, LineFactory>()
        val REGISTRY_KEY = RegistryKey.simple<String, LineFactory>(RegistryId("aquatic", "line-factories"))
    }
}