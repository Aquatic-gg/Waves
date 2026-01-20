package gg.aquatic.statistik.impl

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.statistik.ListenerStatisticType
import gg.aquatic.statistik.StatisticAddEvent
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

object KillStatistic : ListenerStatisticType<Player>() {
    override val arguments: Collection<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("types", ArrayList<String>(), true)
    )

    override fun createListener(): Listener = listen<EntityDamageByEntityEvent> { event ->
        val player = event.damager as? Player ?: return@listen

        for (statisticHandle in handles) {
            val types = statisticHandle.args.stringCollection("types") ?: listOf()
            if ("ALL" !in types && event.entity.type.name.uppercase() !in types) continue

            statisticHandle.consumer(StatisticAddEvent(this, 1, player))
        }
    }
}