package gg.aquatic.statistik.impl

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.statistik.ListenerStatisticType
import gg.aquatic.statistik.StatisticAddEvent
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent

object BlockPlaceStatistic: ListenerStatisticType<Player>() {
    override val arguments: Collection<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("types", ArrayList<String>(), true)
    )

    override fun createListener() = listen<BlockPlaceEvent> {
        val player = it.player

        for (statisticHandle in handles) {
            val args = statisticHandle.args
            val types = args.stringCollection("types") ?: listOf()

            if ("ALL" !in types && it.block.type.name.uppercase() !in types) {
                continue
            }

            val event = StatisticAddEvent(this, 1, player)
            statisticHandle.consumer(event)
        }
    }
}