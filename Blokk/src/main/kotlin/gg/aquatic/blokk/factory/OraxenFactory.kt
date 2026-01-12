package gg.aquatic.blokk.factory

import gg.aquatic.blokk.Blokk
import gg.aquatic.blokk.impl.OraxenBlock
import org.bukkit.configuration.ConfigurationSection

object OraxenFactory : BlockFactory {
    override fun load(section: ConfigurationSection, material: String): Blokk {
        return OraxenBlock(material)
    }
}