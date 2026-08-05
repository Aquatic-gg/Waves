package gg.aquatic.execute.condition.type

import gg.aquatic.execute.Condition
import org.bukkit.command.CommandSender

data class MessageConditionBinder(
    val sender: CommandSender,
    val page: Int,
    val totalPages: Int,
)

abstract class MessageCondition : Condition<MessageConditionBinder> {
    override val binder: Class<out MessageConditionBinder> = MessageConditionBinder::class.java
}
