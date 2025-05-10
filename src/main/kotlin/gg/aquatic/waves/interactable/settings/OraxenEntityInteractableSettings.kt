package gg.aquatic.waves.interactable.settings

import gg.aquatic.waves.fake.entity.FakeEntity
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.interactable.InteractableInteractEvent
import gg.aquatic.waves.interactable.type.EntityInteractable
import gg.aquatic.waves.util.audience.AquaticAudience
import io.th0rgal.oraxen.api.OraxenFurniture
import io.th0rgal.oraxen.api.OraxenItems
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f

class OraxenEntityInteractableSettings(
    val furniture: FurnitureMechanic,
    val offset: Vector
): InteractableSettings {

    override fun build(location: Location, audience: AquaticAudience, onInteract: (InteractableInteractEvent) -> Unit): EntityInteractable {
        val item = OraxenItems.getItemById(furniture.itemID).build()
        val displaySettings = furniture.displayEntityProperties
        val fakeEntity = FakeEntity(EntityType.ITEM_DISPLAY, location.clone().add(offset), 50, audience, consumer =  {
            entityData += "display-data" to object : EntityData {
                override val id: String
                    get() = "display-data"

                override fun apply(entity: Entity, updater: (String) -> String) {
                    val itemDisplay = entity as? org.bukkit.entity.ItemDisplay ?: return
                    itemDisplay.billboard = displaySettings.trackingRotation
                    itemDisplay.itemDisplayTransform = displaySettings.displayTransform
                    itemDisplay.transformation = Transformation(
                        Vector3f(),
                        Quaternionf(),
                        Vector3f(displaySettings.scale.x, displaySettings.scale.y, displaySettings.scale.z),
                        Quaternionf()
                    )
                    itemDisplay.displayWidth = displaySettings.displayWidth
                    itemDisplay.displayHeight = displaySettings.displayHeight
                    itemDisplay.setItemStack(item)
                }
            }
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