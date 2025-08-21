package gg.aquatic.waves.util.statistic.impl

import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.util.statistic.StatisticAddEvent
import gg.aquatic.waves.util.statistic.StatisticType
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

object DeathStatistic: StatisticType<Player>() {
    override val arguments: Collection<AquaticObjectArgument<*>> = listOf()

    private var listener: Listener? = null

    override fun initialize() {
        listener = event<PlayerDeathEvent>(ignoredCancelled = true) {
            val player = it.entity as? Player ?: return@event

            for (statisticHandle in handles) {
                val event = StatisticAddEvent(this, 1, player)
                statisticHandle.consumer(event)
            }
        }
    }

    override fun terminate() {
        listener?.let { HandlerList.unregisterAll(it) }
        listener = null
    }
}