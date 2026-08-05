package gg.aquatic.stacked.option

import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection

interface ItemOption {

    val key: Key

    fun load(section: ConfigurationSection): ItemOptionHandle?

}