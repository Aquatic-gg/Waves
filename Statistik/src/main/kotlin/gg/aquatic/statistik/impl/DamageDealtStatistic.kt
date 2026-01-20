package gg.aquatic.statistik.impl

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.statistik.ListenerStatisticType
import gg.aquatic.statistik.StatisticAddEvent
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent

object DamageDealtStatistic: ListenerStatisticType<Player>() {
    override val arguments: Collection<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("types", ArrayList<String>(), true)
    )

    override fun createListener() = listen<EntityDamageByEntityEvent> {
        val player = it.damager as? Player ?: return@listen

        for (statisticHandle in handles) {
            val args = statisticHandle.args
            val types = args.stringCollection("types") ?: listOf()

            if ("ALL" !in types && it.entity.type.name.uppercase() !in types) {
                continue
            }
            val event = StatisticAddEvent(this, it.damage, player)
            statisticHandle.consumer(event)
        }
    }
}