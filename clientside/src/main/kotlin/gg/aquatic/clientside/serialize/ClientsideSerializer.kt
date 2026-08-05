package gg.aquatic.clientside.serialize

import org.bukkit.configuration.ConfigurationSection

object ClientsideSerializer {

    fun fromSection(section: ConfigurationSection): ClientsideSettings<*>? {
        val type = section.getString("type") ?: return null
        val factory = ClientsideSettings.Factory.REGISTRY[type] ?: return null
        return factory.fromSection(section)
    }

}