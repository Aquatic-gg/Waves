package gg.aquatic.common

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration
import kotlin.collections.iterator

/**
 * Retrieves a list of ConfigurationSection objects from a specified path in the current ConfigurationSection.
 *
 * @param path The path to retrieve the list of ConfigurationSection objects from.
 * @return A list of ConfigurationSection objects extracted from the path. If the path does not exist or contains invalid data,
 *         an empty list is returned.
 */
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

/**
 * Converts a map into a ConfigurationSection, supporting nested maps and lists.
 *
 * @param map The input map to be converted into a ConfigurationSection. The map may
 *            contain nested maps and lists which will be recursively converted.
 * @return A ConfigurationSection representing the contents of the provided map.
 */
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

fun <T> ConfigurationSection.section(consumer: ConfigurationSection.() -> T) =
    consumer(
        this.getOrCreateSection(this.name)
    )


fun ConfigurationSection.getOrCreateSection(path: String): ConfigurationSection {
    return this.getConfigurationSection(path) ?: this.createSection(path)
}