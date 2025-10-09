package gg.aquatic.execute.argument

import org.bukkit.configuration.ConfigurationSection

abstract class AbstractObjectArgumentSerializer<T> {

    abstract fun load(section: ConfigurationSection, id: String): T?

}