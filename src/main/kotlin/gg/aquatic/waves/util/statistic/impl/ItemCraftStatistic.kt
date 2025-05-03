package gg.aquatic.waves.util.statistic.impl

import com.willfp.eco.core.gui.player
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.util.statistic.StatisticAddEvent
import gg.aquatic.waves.util.statistic.StatisticType
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent

object ItemCraftStatistic: StatisticType<Player>() {
    override val arguments: Collection<AquaticObjectArgument<*>> = listOf()

    private var listener: Listener? = null

    override fun initialize() {
        CraftItemEvent.getHandlerList()
        listener = event<CraftItemEvent>(ignoredCancelled = true) {
            val player = it.player as? Player ?: return@event

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