package gg.aquatic.waves.command.impl

import gg.aquatic.waves.command.ICommand
import gg.aquatic.waves.message.MessageSerializer
import gg.aquatic.waves.util.runAsync
import org.bukkit.command.CommandSender

object ReloadMessagesCommand: ICommand {
    override fun run(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("waves.admin")) {
            return
        }

        sender.sendMessage("Reloading messages...")
        runAsync {
            MessageSerializer.loadWavesCustomMessages()
            sender.sendMessage("Messages have been reloaded!")
        }
    }

    override fun tabComplete(
        sender: CommandSender,
        args: Array<out String>,
    ): List<String> {
        return listOf()
    }
}