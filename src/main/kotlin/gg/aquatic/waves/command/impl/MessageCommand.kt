package gg.aquatic.waves.command.impl

import gg.aquatic.waves.command.ICommand
import gg.aquatic.waves.util.message.Messages
import gg.aquatic.waves.util.message.impl.page.PaginatedMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import kotlin.math.ceil

object MessageCommand : ICommand {
    override fun run(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("waves.admin")) {
            return
        }

        if (args.size < 3) {
            sender.sendMessage("Usage: /waves configmessage <player> <message> [page]")
            return
        }

        val player = if (args[1].lowercase() == "*console") {
            Bukkit.getConsoleSender()
        } else Bukkit.getPlayer(args[1])
        if (player == null) {
            sender.sendMessage("Player not found!")
            return
        }
        val message = Messages.registeredMessages[args[2]]
        if (message == null) {
            sender.sendMessage("Message not found!")
            return
        }

        val page = if (args.size >= 4) {
            args[3].toIntOrNull() ?: 0
        } else 0

        if (page > 0) {
            if (message is PaginatedMessage) {
                message.send(player, page)
                return
            }
        }

        message.send(player)
    }

    override fun tabComplete(
        sender: CommandSender,
        args: Array<out String>,
    ): List<String> {
        if (args.size == 1) {
            return Bukkit.getOnlinePlayers().map { it.name } + listOf("*console")
        }
        if (args.size == 2) {
            return Messages.registeredMessages.keys.toList()
        }
        if (args.size == 3) {
            val messageId = args[1]
            val message = Messages.registeredMessages[messageId] ?: return listOf()
            if (message !is PaginatedMessage) return listOf()
            return (0..ceil(message.messages.size.toDouble() / message.pageSize.toDouble()).toInt() - 1).map { it.toString() }
        }
        return listOf()
    }
}