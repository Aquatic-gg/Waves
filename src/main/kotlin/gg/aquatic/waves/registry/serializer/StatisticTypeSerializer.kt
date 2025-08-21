package gg.aquatic.waves.registry.serializer

import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.util.argument.ArgumentSerializer
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.statistic.StatisticAddEvent
import gg.aquatic.waves.util.statistic.StatisticHandle
import gg.aquatic.waves.util.statistic.StatisticType
import org.bukkit.configuration.ConfigurationSection

object StatisticTypeSerializer {

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> fromSection(
        configurationSection: ConfigurationSection,
        noinline consumer: (StatisticAddEvent<T>) -> Unit
    ): StatisticHandle<T>? {
        val typeId = configurationSection.getString("type") ?: return null
        val registry = WavesRegistry.STATISTIC_TYPES[T::class.java] ?: return null
        val type = registry[typeId] as? StatisticType<T> ?: return null
        val loadedArgs = ArgumentSerializer.load(configurationSection, type.arguments)
        val args = ObjectArguments(loadedArgs)

        return StatisticHandle(type, args, consumer)
    }

    inline fun <reified T> fromSections(
        configurationSections: List<ConfigurationSection>,
        noinline consumer: (StatisticAddEvent<T>) -> Unit
    ): List<StatisticHandle<T>> {
        return configurationSections.mapNotNull { fromSection<T>(it, consumer) }
    }

}