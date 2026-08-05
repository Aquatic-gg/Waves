package gg.aquatic.klocale.impl.paper

import org.bukkit.command.CommandSender

interface MessageContext {
    val sender: CommandSender
}

data class SimpleMessageContext(
    override val sender: CommandSender,
) : MessageContext

data class PaginatedMessageContext(
    override val sender: CommandSender,
    val page: Int,
    val totalPages: Int,
) : MessageContext
