package gg.aquatic.execute.condition.type

import gg.aquatic.execute.Condition
import org.bukkit.entity.Player

abstract class PlayerCondition: Condition<Player> {
    override val binder: Class<out Player> = Player::class.java
}