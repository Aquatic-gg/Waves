package gg.aquatic.clientside.serialize

import gg.aquatic.clientside.ObjectInteractEvent
import gg.aquatic.clientside.entity.FakeEntity
import gg.aquatic.common.audience.AquaticAudience
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType

class ClientsideEntitySettings(
    val entityType: EntityType,
    val viewRange: Int
) : ClientsideSettings<FakeEntity> {
    override fun create(
        location: Location,
        audience: AquaticAudience,
        onInteract: ObjectInteractEvent<FakeEntity>
    ): FakeEntity {
        return FakeEntity(entityType, location, viewRange, audience, onInteract = onInteract)
    }

    companion object: ClientsideSettings.Factory {
        override fun fromSection(section: ConfigurationSection): ClientsideSettings<*>? {
            val entityType = EntityType.valueOf(section.getString("entity") ?: return null)
            val viewRange = section.getInt("view-range", 50)
            return ClientsideEntitySettings(entityType, viewRange)
        }

    }
}