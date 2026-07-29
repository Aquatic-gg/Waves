package gg.aquatic.blokk.factory

import gg.aquatic.blokk.Blokk
import gg.aquatic.blokk.bootstrapHolder
import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey
import org.bukkit.configuration.ConfigurationSection

interface BlockFactory {

    fun load(section: ConfigurationSection, material: String): Blokk?

    companion object {
        val REGISTRY_KEY =
            RegistryKey.simple<String, BlockFactory>(RegistryId("aquatic", "block_factory"))
        val REGISTRY: Registry<String, BlockFactory>
            get() {
                return bootstrapHolder[REGISTRY_KEY]
            }
    }
}