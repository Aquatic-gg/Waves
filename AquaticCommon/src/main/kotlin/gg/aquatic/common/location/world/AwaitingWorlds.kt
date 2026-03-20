package gg.aquatic.common.location.world

import gg.aquatic.common.event
import org.bukkit.event.world.WorldLoadEvent

object AwaitingWorlds {

    val awaiting = HashMap<String, AwaitingWorld>()

    fun initialize() {
        event<WorldLoadEvent> {
            val awaitingWorld = awaiting.remove(it.world.name) ?: return@event
            awaitingWorld.thens.forEach { then -> then(it.world) }
        }
    }
}
