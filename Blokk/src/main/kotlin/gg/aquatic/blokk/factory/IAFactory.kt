package gg.aquatic.blokk.factory

import gg.aquatic.blokk.Blokk
import gg.aquatic.blokk.impl.ItemsAdderBlock
import org.bukkit.configuration.ConfigurationSection

object IAFactory : BlockFactory {
    override fun load(section: ConfigurationSection, material: String): Blokk? {
        return ItemsAdderBlock(material)
    }
}