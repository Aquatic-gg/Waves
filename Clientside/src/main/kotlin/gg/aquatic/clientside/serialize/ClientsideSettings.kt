package gg.aquatic.clientside.serialize

import gg.aquatic.clientside.FakeObject
import gg.aquatic.clientside.FakeObjectHandler.bootstrapHolder
import gg.aquatic.clientside.ObjectInteractEvent
import gg.aquatic.common.audience.AquaticAudience
import gg.aquatic.common.toBlockCardinal
import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector

interface ClientsideSettings<T: FakeObject> {

    suspend fun create(location: Location, audience: AquaticAudience, onInteract: ObjectInteractEvent<T>): T

    fun applyOffset(
        location: Location,
        offsetX: Double,
        offsetY: Double,
        offsetZ: Double,
        centerOnBlockXZ: Boolean = false
    ): Location {
        val face = location.yaw.toBlockCardinal()
        val rotatedOffset = Vector(offsetX, offsetY, offsetZ)
            .rotateAroundY(-Math.toRadians(face.ordinal * 90.0))

        return location.block.location.clone().apply {
            yaw = location.yaw
            pitch = location.pitch
            if (centerOnBlockXZ) {
                add(0.5, 0.0, 0.5)
            }
            add(rotatedOffset)
        }
    }

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
