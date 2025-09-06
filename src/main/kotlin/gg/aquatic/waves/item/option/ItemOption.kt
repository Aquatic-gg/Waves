package gg.aquatic.waves.item.option

import org.bukkit.configuration.ConfigurationSection

interface ItemOption {

    fun load(section: ConfigurationSection): ItemOptionHandle?

}