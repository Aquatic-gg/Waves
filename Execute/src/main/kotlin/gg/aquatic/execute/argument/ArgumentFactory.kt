package gg.aquatic.execute.argument

import org.bukkit.configuration.ConfigurationSection

abstract class ArgumentFactory<T> {

    abstract fun load(section: ConfigurationSection, id: String): T?

}