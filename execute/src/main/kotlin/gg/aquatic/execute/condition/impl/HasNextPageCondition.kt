package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.execute.condition.type.MessageCondition
import gg.aquatic.execute.condition.type.MessageConditionBinder

object HasNextPageCondition : MessageCondition() {
    override suspend fun execute(binder: MessageConditionBinder, args: ArgumentContext<MessageConditionBinder>): Boolean {
        return binder.page < binder.totalPages - 1
    }

    override val arguments: List<ObjectArgument<*>> = emptyList()
}
