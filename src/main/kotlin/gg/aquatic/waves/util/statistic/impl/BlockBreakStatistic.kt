package gg.aquatic.waves.util.statistic.impl

import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.util.statistic.StatisticAddEvent
import gg.aquatic.waves.util.statistic.StatisticType
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

object BlockBreakStatistic: StatisticType<Player>() {
    override val arguments: Collection<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("types", ArrayList<String>(), true)
    )

    private var listener: Listener? = null

    override fun initialize() {
        listener = event<BlockBreakEvent>(ignoredCancelled = true) {
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

    override fun terminate() {
        listener?.let { HandlerList.unregisterAll(it) }
        listener = null
    }
}