package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.condition.type.MessageCondition
import gg.aquatic.execute.condition.type.MessageConditionBinder

object SenderPermissionCondition : MessageCondition() {
    override suspend fun execute(binder: MessageConditionBinder, args: ArgumentContext<MessageConditionBinder>): Boolean {
        val permission = args.string("permission") ?: return false
        return binder.sender.hasPermission(permission)
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("permission", "example.permission", true)
    )
}
