package gg.aquatic.blokk.factory

import gg.aquatic.blokk.AquaticBlock
import gg.aquatic.kregistry.FrozenRegistry
import gg.aquatic.kregistry.Registry
import gg.aquatic.kregistry.RegistryId
import gg.aquatic.kregistry.RegistryKey
import org.bukkit.configuration.ConfigurationSection

interface BlockFactory {

    fun load(section: ConfigurationSection, material: String): AquaticBlock?

    companion object {
        val REGISTRY_KEY = RegistryKey<String, BlockFactory>(RegistryId("aquatic","block_factory"))
        val REGISTRY: FrozenRegistry<String, BlockFactory>
            get() {
                return Registry[REGISTRY_KEY]
            }
    }
}