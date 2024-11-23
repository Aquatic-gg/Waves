package gg.aquatic.waves.interactable.settings

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import gg.aquatic.aquaticseries.lib.audience.AquaticAudience
import gg.aquatic.aquaticseries.lib.util.getSectionList
import gg.aquatic.aquaticseries.lib.util.mapPair
import gg.aquatic.waves.fake.entity.FakeEntity
import gg.aquatic.waves.interactable.Interactable
import gg.aquatic.waves.interactable.InteractableInteractEvent
import gg.aquatic.waves.interactable.settings.entityproperty.EntityProperty
import gg.aquatic.waves.interactable.type.EntityInteractable
import gg.aquatic.waves.packetevents.EntityDataBuilder
import gg.aquatic.waves.registry.serializer.EntityPropertySerializer
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection

class EntityInteractableSettings(
    val props: HashSet<EntityProperty>
): InteractableSettings {
    override fun build(
        location: Location,
        audience: AquaticAudience,
        onInteract: (InteractableInteractEvent) -> Unit
    ): Interactable {
        val fakeEntity = FakeEntity(EntityTypes.ITEM_DISPLAY, location, 50, audience, consumer =  {
            val builder = EntityDataBuilder.ANY
            for (prop in props) {
                prop.apply(builder)
            }
            entityData += builder.build().mapPair { it.index to it }
        })

        val interactable = EntityInteractable(fakeEntity, onInteract)
        return interactable
    }

    companion object: InteractableSettingsFactory {
        override fun load(section: ConfigurationSection): InteractableSettings {
            val props = EntityPropertySerializer.fromSections(section.getSectionList("properties")).toHashSet()
            return EntityInteractableSettings(props)
        }

    }
}