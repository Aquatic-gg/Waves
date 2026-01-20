package gg.aquatic.statistik.impl

import gg.aquatic.common.AquaticCommon
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.statistik.StatisticAddEvent
import gg.aquatic.statistik.StatisticHandle
import gg.aquatic.statistik.StatisticType
import gg.aquatic.treepapi.updatePAPIPlaceholders
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object PlaceholderStatistic : StatisticType<Player>() {
    override val arguments: Collection<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("placeholder", "", true),
        PrimitiveObjectArgument("update", 20, false)
    )

    private val cache = HashMap<String, Cache>()

    override fun initialize() {
        for (statisticHandle in handles) {
            register(statisticHandle)
        }
    }

    override fun terminate() {
        cache.values.forEach { it.stop() }
        cache.clear()
    }

    private fun register(statisticHandle: StatisticHandle<Player>) {
        val args = statisticHandle.args
        val placeholder = args.string("placeholder") ?: return
        val update = args.int("update") ?: return

        val cache = Cache(statisticHandle, update, placeholder)

        this.cache["$placeholder:$update"] = cache
    }

    override fun onRegister(handle: StatisticHandle<Player>) {
        register(handle)
    }

    class Cache(
        val handle: StatisticHandle<Player>,
        interval: Int,
        val placeholder: String,
    ) {

        val map = HashMap<UUID, Float>()
        val runnable = object : BukkitRunnable() {
            override fun run() {
                check()
            }
        }.runTaskTimer(AquaticCommon.plugin, 0L, interval.toLong())

        fun check() {
            for (player in Bukkit.getOnlinePlayers()) {
                val value = placeholder.updatePAPIPlaceholders(player).toFloatOrNull() ?: continue
                val previousValue = map[player.uniqueId]
                map[player.uniqueId] = value

                if (previousValue != null) {
                    val difference = value - previousValue
                    val event = StatisticAddEvent(PlaceholderStatistic, difference, player)
                    handle.consumer(event)
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