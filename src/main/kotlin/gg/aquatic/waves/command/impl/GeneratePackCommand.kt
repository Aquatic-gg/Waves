package gg.aquatic.waves.command.impl

import gg.aquatic.waves.command.ICommand
import gg.aquatic.waves.pack.PackHandler
import gg.aquatic.waves.util.runAsync
import org.bukkit.command.CommandSender

object GeneratePackCommand: ICommand {
    override fun run(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("waves.admin")) {
            return
        }

        sender.sendMessage("Generating pack...")
        runAsync {
            PackHandler.loadPack()

            sender.sendMessage("Pack has been generated!")
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return listOf()
    }
}