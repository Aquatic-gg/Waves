package gg.aquatic.waves.util.statistic.impl

import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.util.statistic.StatisticAddEvent
import gg.aquatic.waves.util.statistic.StatisticType
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

object TravelStatistic: StatisticType<Player>() {
    override val arguments: Collection<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("count-swimming", true, required = true),
        PrimitiveObjectArgument("count-flying", true, required = true),
        PrimitiveObjectArgument("count-gliding", defaultValue = true, required = true),
        PrimitiveObjectArgument("count-driving", defaultValue = true, required = true)
    )
    private var listener: Listener? = null

    override fun initialize() {
        PlayerMoveEvent.getHandlerList()
        listener = event<PlayerMoveEvent>(ignoredCancelled = true) {
            val player = it.player

            if (it.from.blockX == it.to?.blockX && it.from.blockY == it.to?.blockY && it.from.blockZ == it.to?.blockZ) {
                return@event
            }

            for (statisticHandle in handles) {
                val args = statisticHandle.args
                val swimming = args.boolean("count-swimming") ?: true
                val flying = args.boolean("count-flying") ?: true
                val gliding = args.boolean("count-gliding") ?: true
                val driving = args.boolean("count-driving") ?: true

                if ((!swimming && player.isSwimming) ||
                    (!flying && player.isFlying) ||
                    (!gliding && player.isGliding) ||
                    (!driving && player.isInsideVehicle)) {
                    continue
                }

                val event = StatisticAddEvent(this, it.from.distance(it.to!!), player)
                statisticHandle.consumer(event)
            }
        }
    }

    override fun terminate() {
        listener?.let { HandlerList.unregisterAll(it) }
        listener = null
    }
}