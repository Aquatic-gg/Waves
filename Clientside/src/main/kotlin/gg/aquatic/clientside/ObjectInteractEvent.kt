package gg.aquatic.clientside

import org.bukkit.entity.Player

fun interface ObjectInteractEvent<T: FakeObject> {
    fun onInteract(obj: T, player: Player, isLeftClick: Boolean)
}