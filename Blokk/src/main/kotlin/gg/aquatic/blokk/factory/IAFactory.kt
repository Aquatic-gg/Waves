package gg.aquatic.blokk.factory

import gg.aquatic.blokk.AquaticBlock
import gg.aquatic.blokk.impl.ItemsAdderBlock
import org.bukkit.configuration.ConfigurationSection

object IAFactory : BlockFactory {
    override fun load(section: ConfigurationSection, material: String): AquaticBlock? {
        return ItemsAdderBlock(material)
    }
}