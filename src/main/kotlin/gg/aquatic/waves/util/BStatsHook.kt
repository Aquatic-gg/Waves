package gg.aquatic.waves.util

import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

fun JavaPlugin.registerMetrics(id: Int): Metrics {
    val metrics = Metrics(this, id)
    return metrics
}