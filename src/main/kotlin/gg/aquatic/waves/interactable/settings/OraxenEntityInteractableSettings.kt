package gg.aquatic.waves.interactable.settings

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import gg.aquatic.waves.util.audience.AquaticAudience
import gg.aquatic.waves.fake.entity.FakeEntity
import gg.aquatic.waves.interactable.InteractableInteractEvent
import gg.aquatic.waves.interactable.type.EntityInteractable
import gg.aquatic.waves.packetevents.EntityDataBuilder
import gg.aquatic.waves.util.collection.mapPair
import io.th0rgal.oraxen.api.OraxenFurniture
import io.th0rgal.oraxen.api.OraxenItems
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.util.Vector

class OraxenEntityInteractableSettings(
    val furniture: FurnitureMechanic,
    val offset: Vector
): InteractableSettings {

    override fun build(location: Location, audience: AquaticAudience, onInteract: (InteractableInteractEvent) -> Unit): EntityInteractable {
        val item = OraxenItems.getItemById(furniture.itemID).build()
        val displaySettings = furniture.displayEntityProperties
        val fakeEntity = FakeEntity(EntityType.ITEM_DISPLAY, location.clone().add(offset), 50, audience, consumer =  {
            entityData += EntityDataBuilder.ITEM_DISPLAY()
                .setItem(item)
                .setItemTransformation(displaySettings.displayTransform)
                .setScale(displaySettings.scale.x, displaySettings.scale.y, displaySettings.scale.z)
                .setBillboard(displaySettings.trackingRotation)
                .setWidth(displaySettings.displayWidth)
                .setHeight(displaySettings.displayHeight)
                .build()
                .mapPair { it.index to it }
        })

        val interactable = EntityInteractable(fakeEntity, onInteract)
        return interactable
    }

    companion object: InteractableSettingsFactory {
        override fun load(section: ConfigurationSection): InteractableSettings? {
            val id = section.getString("id")
            val furnitureMechanic = OraxenFurniture.getFurnitureMechanic(id) ?: return null
            val offsetStrs = section.getString("offset", "0;0;0")!!.split(";")
            val offset = Vector(
                offsetStrs.getOrElse(0) { "0" }.toDouble(),
                offsetStrs.getOrElse(1) { "0" }.toDouble(),
                offsetStrs.getOrElse(2) { "0" }.toDouble()
            )
            return OraxenEntityInteractableSettings(furnitureMechanic, offset)
        }
    }

}