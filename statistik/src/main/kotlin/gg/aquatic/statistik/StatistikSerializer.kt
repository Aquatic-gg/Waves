package gg.aquatic.statistik

import gg.aquatic.common.argument.ArgumentSerializer
import gg.aquatic.common.argument.ObjectArguments
import gg.aquatic.statistik.StatisticType.Companion.getHierarchical
import org.bukkit.configuration.ConfigurationSection

object StatistikSerializer {

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T: Any> fromSection(
        configurationSection: ConfigurationSection,
        noinline consumer: (StatisticAddEvent<T>) -> Unit
    ): StatisticHandle<T>? {
        val typeId = configurationSection.getString("type") ?: return null
        val type = StatisticType.REGISTRY.getHierarchical(typeId, T::class.java) ?: return null
        val loadedArgs = ArgumentSerializer.load(configurationSection, type.arguments)
        val args = ObjectArguments(loadedArgs)

        return StatisticHandle(type, args, consumer)
    }

    inline fun <reified T: Any> fromSections(
        configurationSections: List<ConfigurationSection>,
        noinline consumer: (StatisticAddEvent<T>) -> Unit
    ): List<StatisticHandle<T>> {
        return configurationSections.mapNotNull { fromSection<T>(it, consumer) }
    }

}