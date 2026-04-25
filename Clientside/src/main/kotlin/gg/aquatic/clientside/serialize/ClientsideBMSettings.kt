package gg.aquatic.clientside.serialize

import gg.aquatic.clientside.ObjectInteractEvent
import gg.aquatic.clientside.bm.FakeBM
import gg.aquatic.common.audience.AquaticAudience
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection

class ClientsideBMSettings(
    val modelId: String,
    val viewRange: Int,
    val offsetX: Double = 0.0,
    val offsetY: Double = 0.0,
    val offsetZ: Double = 0.0,
    val scale: Double = 1.0,
    val glowing: Boolean = false,
) : ClientsideSettings<FakeBM> {
    override suspend fun create(
        location: Location,
        audience: AquaticAudience,
        onInteract: ObjectInteractEvent<FakeBM>
    ): FakeBM {
        return FakeBM.createRegistered(
            location = applyOffset(location, offsetX, offsetY, offsetZ, centerOnBlockXZ = true),
            modelId = modelId,
            viewRange = viewRange,
            initialAudience = audience,
            onInteract = onInteract,
        ).apply {
            setScale(scale)
            setGlowing(glowing)
        }
    }

    companion object : ClientsideSettings.Factory {
        override fun fromSection(section: ConfigurationSection): ClientsideBMSettings {
            val modelId = section.getString("model") ?: throw IllegalArgumentException("modelId is required")
            val viewRange = section.getInt("view-range", 50)
            return ClientsideBMSettings(
                modelId = modelId,
                viewRange = viewRange,
                offsetX = section.getDouble("offset.x", 0.0),
                offsetY = section.getDouble("offset.y", 0.0),
                offsetZ = section.getDouble("offset.z", 0.0),
                scale = section.getDouble("scale", 1.0),
                glowing = section.getBoolean("glowing", false),
            )
        }
    }
}
