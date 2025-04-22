package gg.aquatic.waves.util

import gg.aquatic.waves.Waves
import org.bstats.bukkit.Metrics

object BStatsUtils {

    fun registerMetrics(id: Int): Metrics {
        val metrics = Metrics(Waves.INSTANCE, id)
        return metrics
    }

}