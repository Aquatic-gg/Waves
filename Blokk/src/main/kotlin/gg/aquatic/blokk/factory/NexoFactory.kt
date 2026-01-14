package gg.aquatic.blokk.factory

import gg.aquatic.blokk.Blokk
import gg.aquatic.blokk.impl.NexoBlock
import org.bukkit.configuration.ConfigurationSection

object NexoFactory : BlockFactory {
    override fun load(section: ConfigurationSection, material: String): Blokk {
        return NexoBlock(material)
    }
}