package gg.aquatic.waves.command.impl

import gg.aquatic.waves.command.ICommand
import gg.aquatic.waves.util.item.ItemEncoder
import gg.aquatic.waves.util.toMMComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ItemConvertCommand : ICommand {
    override fun run(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("waves.admin")) {
            return
        }
        if (sender !is Player) return

        val item = sender.inventory.itemInMainHand
        if (item.type.isAir) {
            sender.sendMessage("You must be holding an item to convert!")
            return
        }
        val base64 = ItemEncoder.encode(item)
        sender.sendMessage("Item has been converted! (<u><click:COPY_TO_CLIPBOARD:'$base64'>Click to copy</click></u>)".toMMComponent())
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return listOf()
    }
}