package gg.aquatic.statistik.impl

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.statistik.ListenerStatisticType
import gg.aquatic.statistik.StatisticAddEvent
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent

object DeathStatistic: ListenerStatisticType<Player>() {
    override val arguments: Collection<ObjectArgument<*>> = listOf()

    override fun createListener() = listen<PlayerDeathEvent> {
        val player = it.entity

        for (statisticHandle in handles) {
            val event = StatisticAddEvent(this, 1, player)
            statisticHandle.consumer(event)
        }
    }
}