package gg.aquatic.waves.interactable.settings.entityproperty

import gg.aquatic.waves.item.AquaticItem
import org.bukkit.configuration.ConfigurationSection

class EntityArmorProperty(
    val helmet: AquaticItem?,
    val chestplate: AquaticItem?,
    val leggings: AquaticItem?,
    val boots: AquaticItem?,
    val mainHand: AquaticItem?,
    val offHand: AquaticItem?
)  {

    object Serializer {
        fun load(section: ConfigurationSection): EntityArmorProperty {
            val helmet = AquaticItem.loadFromYml(section.getConfigurationSection("armor.helmet"))
            val chestplate = AquaticItem.loadFromYml(section.getConfigurationSection("armor.chestplate"))
            val leggings = AquaticItem.loadFromYml(section.getConfigurationSection("armor.leggings"))
            val boots = AquaticItem.loadFromYml(section.getConfigurationSection("armor.boots"))
            val mainHand = AquaticItem.loadFromYml(section.getConfigurationSection("armor.main-hand"))
            val offHand = AquaticItem.loadFromYml(section.getConfigurationSection("armor.off-hand"))
            return EntityArmorProperty(helmet, chestplate, leggings, boots, mainHand, offHand)
        }
    }
}