package gg.aquatic.clientside.serialize

import gg.aquatic.clientside.ObjectInteractEvent
import gg.aquatic.clientside.meg.FakeMEG
import gg.aquatic.common.audience.AquaticAudience
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection

class ClientsideMEGSettings(
    val modelId: String,
    val viewRange: Int,
): ClientsideSettings<FakeMEG> {
    override fun create(
        location: Location,
        audience: AquaticAudience,
        onInteract: ObjectInteractEvent<FakeMEG>
    ): FakeMEG {
        return FakeMEG(location, modelId, viewRange, audience, onInteract)
    }

    companion object: ClientsideSettings.Factory {
        override fun fromSection(section: ConfigurationSection): ClientsideMEGSettings {
            val modelId = section.getString("model") ?: throw IllegalArgumentException("modelId is required")
            val viewRange = section.getInt("view-range", 50)
            return ClientsideMEGSettings(modelId, viewRange)
        }
    }
}