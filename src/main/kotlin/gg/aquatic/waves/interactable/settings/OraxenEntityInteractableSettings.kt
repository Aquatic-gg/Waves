package gg.aquatic.waves.interactable.settings

import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.fake.entity.FakeEntity
import gg.aquatic.waves.fake.entity.data.impl.display.DisplayEntityData
import gg.aquatic.waves.fake.entity.data.impl.display.ItemDisplayEntityData
import gg.aquatic.waves.interactable.InteractableInteractEvent
import gg.aquatic.waves.interactable.type.EntityInteractable
import gg.aquatic.waves.util.audience.AquaticAudience
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
            val data = ArrayList<EntityDataValue>()
            data += DisplayEntityData.Billboard.generate(displaySettings.trackingRotation)
            data += ItemDisplayEntityData.ItemDisplayTransform.generate(displaySettings.displayTransform)
            data += DisplayEntityData.Scale.generate(displaySettings.scale)
            data += ItemDisplayEntityData.Item.generate(item)
            data += DisplayEntityData.Width.generate(displaySettings.displayWidth)
            data += DisplayEntityData.Height.generate(displaySettings.displayHeight)

            setEntityData(data)
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