package gg.aquatic.waves.interactable.settings

import gg.aquatic.waves.fake.entity.FakeEntity
import gg.aquatic.waves.fake.entity.data.ConfiguredEntityData
import gg.aquatic.waves.interactable.Interactable
import gg.aquatic.waves.interactable.InteractableInteractEvent
import gg.aquatic.waves.interactable.settings.entityproperty.EntityArmorProperty
import gg.aquatic.waves.interactable.type.EntityInteractable
import gg.aquatic.waves.registry.serializer.EntityDataSerializer
import gg.aquatic.waves.util.audience.AquaticAudience
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector

class EntityInteractableSettings(
    val entityType: EntityType,
    val props: HashSet<ConfiguredEntityData>,
    val offset: Vector,
    val yawPitch: Pair<Float, Float>,
    val equipment: EntityArmorProperty,
) : InteractableSettings {
    override fun build(
        location: Location,
        audience: AquaticAudience,
        onInteract: (InteractableInteractEvent) -> Unit,
    ): Interactable {
        val fakeEntity = FakeEntity(entityType, location.clone().add(offset).apply {
            yaw = yawPitch.first
            pitch = yawPitch.second
        }, 50, audience, consumer = {
            setEntityData(props.flatMap { it.generate { str -> str } }.toList())
            this@EntityInteractableSettings.equipment.helmet?.getItem()?.let { equipment += EquipmentSlot.HEAD to it }
            this@EntityInteractableSettings.equipment.chestplate?.getItem()
                ?.let { equipment += EquipmentSlot.CHEST to it }
            this@EntityInteractableSettings.equipment.leggings?.getItem()
                ?.let { equipment += EquipmentSlot.LEGS to it }
            this@EntityInteractableSettings.equipment.boots?.getItem()?.let { equipment += EquipmentSlot.FEET to it }
            this@EntityInteractableSettings.equipment.mainHand?.getItem()
                ?.let { equipment += EquipmentSlot.HAND to it }
            this@EntityInteractableSettings.equipment.offHand?.getItem()
                ?.let { equipment += EquipmentSlot.OFF_HAND to it }
        })

        fakeEntity.register()

        val interactable = EntityInteractable(fakeEntity, onInteract)
        return interactable
    }

    companion object : InteractableSettingsFactory {
        override fun load(section: ConfigurationSection): InteractableSettings? {

            val entityType = section.getString("type")?.let { EntityType.valueOf(it.uppercase()) } ?: return null

            val props = section.getConfigurationSection("properties")?.let {
                EntityDataSerializer.load(it, entityType)
            } ?: emptyList()

            val offsetStrs = section.getString("offset", "0;0;0")!!.split(";")
            val offset = Vector(
                offsetStrs.getOrElse(0) { "0" }.toDouble(),
                offsetStrs.getOrElse(1) { "0" }.toDouble(),
                offsetStrs.getOrElse(2) { "0" }.toDouble()
            )

            val equipment = EntityArmorProperty.Serializer.load(section)
            val yawPitch = (
                    offsetStrs.getOrElse(3) { "0" }.toFloat()
                    ) to (
                    offsetStrs.getOrElse(4) { "0" }.toFloat())
            return EntityInteractableSettings(entityType, props.toHashSet(), offset, yawPitch, equipment)
        }

    }
}