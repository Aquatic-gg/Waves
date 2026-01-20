package gg.aquatic.common.argument

import org.bukkit.configuration.ConfigurationSection

abstract class ObjectArgumentFactory<T> {

    abstract fun load(section: ConfigurationSection, id: String): T?

}