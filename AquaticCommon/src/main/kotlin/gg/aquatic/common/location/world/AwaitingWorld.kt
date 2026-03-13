package gg.aquatic.common.location.world

import gg.aquatic.common.location.LazyLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

class AwaitingWorld private constructor(
    val id: String,
    val thens: MutableCollection<(World) -> Unit>
) {

    companion object {
        fun create(id: String, then: (World) -> Unit) {
            val awaiting = AwaitingWorlds.awaiting[id]
            if (awaiting != null) {
                awaiting.thens.add(then)
            } else {
                AwaitingWorld(id, mutableListOf(then))
            }
        }
    }

    init {
        val world = Bukkit.getWorld(id)
        if (world != null) {
            for (function in thens) {
                function(world)
            }
        } else {
            AwaitingWorlds.awaiting[id] = this
        }
    }
}