package gg.aquatic.kholograms.serialize

import gg.aquatic.kholograms.CommonHologramLineSettings
import gg.aquatic.kregistry.RegistryId
import gg.aquatic.kregistry.RegistryKey
import org.bukkit.configuration.ConfigurationSection

interface LineFactory {

    fun load(section: ConfigurationSection, commonOptions: CommonHologramLineSettings): LineSettings?

    companion object {
        val REGISTRY = mutableMapOf<String, LineFactory>()
        val REGISTRY_KEY = RegistryKey<String, LineFactory>(RegistryId("aquatic", "line-factories"))
    }
}