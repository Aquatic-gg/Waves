package gg.aquatic.waves.util

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration

fun ConfigurationSection.keysForEach(path: String, boolean: Boolean, function: (String) -> Unit) {
    val section = getConfigurationSection(path) ?: return
    section.keysForEach(boolean, function)
}

fun ConfigurationSection.keysForEach(boolean: Boolean, function: (String) -> Unit) {
    getKeys(boolean).forEach(function)
}

fun ConfigurationSection.getSectionList(path: String): List<ConfigurationSection> {
    val list = mutableListOf<ConfigurationSection>()
    val objectList = this.getList(path) ?: return list

    for (obj in objectList) {
        if (obj is ConfigurationSection) {
            list.add(obj)
        } else if (obj is Map<*, *>) {
            list.add(createConfigurationSectionFromMap(obj))
        }
    }
    return list
}

fun createConfigurationSectionFromMap(map: Map<*, *>): ConfigurationSection {
    val mc = MemoryConfiguration()
    for ((key, value) in map) {
        when (value) {
            is Map<*, *> -> {
                mc.createSection(key.toString(), createConfigurationSectionFromMap(value).getValues(false))
            }
            is List<*> -> {
                mc[key.toString()] = value.map { item ->
                    if (item is Map<*, *>) {
                        createConfigurationSectionFromMap(item).getValues(false)
                    } else item
                }
            }
            else -> {
                mc[key.toString()] = value
            }
        }
    }
    return mc
}


fun ConfigurationSection.getOrCreateSection(path: String): ConfigurationSection {
    return this.getConfigurationSection(path) ?: this.createSection(path)
}