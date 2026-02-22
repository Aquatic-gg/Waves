package gg.aquatic.clientside.serialize

import gg.aquatic.clientside.FakeObject
import gg.aquatic.clientside.FakeObjectHandler.bootstrapHolder
import gg.aquatic.clientside.ObjectInteractEvent
import gg.aquatic.common.audience.AquaticAudience
import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection

interface ClientsideSettings<T: FakeObject> {

    fun create(location: Location, audience: AquaticAudience, onInteract: ObjectInteractEvent<T>): T

    interface Factory {
        fun fromSection(section: ConfigurationSection): ClientsideSettings<*>?

        companion object {
            val REGISTRY_KEY = RegistryKey.simple<String, Factory>(RegistryId("aquatic", "clientside_settings_factory"))
            val REGISTRY: Registry<String, Factory>
                get() {
                    return bootstrapHolder[REGISTRY_KEY]
                }
        }
    }
}