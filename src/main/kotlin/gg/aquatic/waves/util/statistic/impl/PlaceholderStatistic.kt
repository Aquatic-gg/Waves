package gg.aquatic.waves.util.statistic.impl

import gg.aquatic.waves.Waves
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.statistic.StatisticAddEvent
import gg.aquatic.waves.util.statistic.StatisticType
import gg.aquatic.waves.util.updatePAPIPlaceholders
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object PlaceholderStatistic : StatisticType<Player>() {
    override val arguments: Collection<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("placeholder", "", true),
        PrimitiveObjectArgument("update", 20, false)
    )

    private val cache = HashMap<String, Cache>()

    override fun initialize() {
        for (statisticHandle in handles) {
            val args = statisticHandle.args
            val placeholder = args.string("placeholder") ?: continue
            val update = args.int("update") ?: continue

            val cache = Cache(update, placeholder)

            this.cache["$placeholder:$update"] = cache
        }
    }

    override fun terminate() {
        cache.values.forEach { it.stop() }
        cache.clear()
    }

    class Cache(
        interval: Int,
        val placeholder: String
    ) {

        val map = HashMap<UUID, Float>()
        val runnable = object : BukkitRunnable() {
            override fun run() {
                check()
            }
        }.runTaskTimer(Waves.INSTANCE, 0L, interval.toLong())

        fun check() {
            for (player in Bukkit.getOnlinePlayers()) {
                val value = placeholder.updatePAPIPlaceholders(player).toFloatOrNull() ?: continue
                val previousValue = map[player.uniqueId]
                map[player.uniqueId] = value

                if (previousValue != null) {
                    val difference = value - previousValue
                    StatisticAddEvent(PlaceholderStatistic, difference, player)
                }
            }
        }

        fun stop() {
            check()
            runnable.cancel()
            map.clear()
        }
    }
}