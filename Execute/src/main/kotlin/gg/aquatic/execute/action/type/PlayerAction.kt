package gg.aquatic.execute.action.type

import gg.aquatic.execute.Action
import org.bukkit.entity.Player

abstract class PlayerAction: Action<Player> {
    override val binder: Class<out Player> = Player::class.java
}