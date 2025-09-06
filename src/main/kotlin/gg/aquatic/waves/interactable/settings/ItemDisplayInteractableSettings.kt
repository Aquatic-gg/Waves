package gg.aquatic.waves.interactable.settings

import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.fake.entity.FakeEntity
import gg.aquatic.waves.fake.entity.data.impl.display.DisplayEntityData
import gg.aquatic.waves.fake.entity.data.impl.display.ItemDisplayEntityData
import gg.aquatic.waves.interactable.Interactable
import gg.aquatic.waves.interactable.InteractableInteractEvent
import gg.aquatic.waves.interactable.type.EntityInteractable
import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.util.audience.AquaticAudience
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.util.Vector
import org.joml.Vector3f

class ItemDisplayInteractableSettings(
    val offset: Vector,
    val item: AquaticItem,
    val itemTransform: ItemDisplayTransform,
    val scale: Vector,
    val billboard: Billboard
) : InteractableSettings {
    override fun build(
        location: Location,
        audience: AquaticAudience,
        onInteract: (InteractableInteractEvent) -> Unit
    ): Interactable {
        val fakeEntity = FakeEntity(EntityType.ITEM_DISPLAY, location.clone().add(offset), 50, audience, consumer = {
            val data = ArrayList<EntityDataValue>()
            data += DisplayEntityData.Billboard.generate(billboard)
            data += ItemDisplayEntityData.ItemDisplayTransform.generate(itemTransform)
            data += DisplayEntityData.Scale.generate(Vector3f(scale.x.toFloat(), scale.y.toFloat(), scale.z.toFloat()))
            data += ItemDisplayEntityData.Item.generate(item.getItem())
            setEntityData(data)
        })

        val interactable = EntityInteractable(fakeEntity, onInteract)
        return interactable
    }

    companion object : InteractableSettingsFactory {
        override fun load(section: ConfigurationSection): InteractableSettings? {
            val offsetStr = section.getString("offset", "0;0;0")!!.split(";")
            val offset = Vector(
                offsetStr[0].toDouble(),
                offsetStr[1].toDouble(),
                offsetStr[2].toDouble()
            )
            val item = AquaticItem.loadFromYml(section.getConfigurationSection("item")) ?: return null
            val itemTransform = ItemDisplayTransform.valueOf(section.getString("item-transform") ?: "NONE")
            val scaleStr = section.getString("scale", "1;1;1")!!.split(";")
            val scale = Vector(
                scaleStr[0].toDouble(),
                scaleStr[1].toDouble(),
                scaleStr[2].toDouble()
            )
            val billboard = Billboard.valueOf(section.getString("billboard") ?: "FIXED")
            return ItemDisplayInteractableSettings(offset, item, itemTransform, scale, billboard)
        }

    }
}