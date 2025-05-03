package gg.aquatic.waves.command

import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class AquaticBaseCommand(
    name: String,
    description: String,
    aliases: MutableList<String>,
    val subCommands: MutableMap<String,ICommand>,
    val helpMessage: List<Component>
) : Command(name, description, "/$name", aliases) {

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            helpMessage.forEach { sender.sendMessage(it) }
            return true
        }

        val cmd = subCommands[args[0]]
        if (cmd == null) {
            helpMessage.forEach { sender.sendMessage(it) }
            return true
        }
        cmd.run(sender, args)
        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
        if (args.size < 2) {
            return subCommands.keys.toList()
        }

        val cmd = subCommands[args[0]] ?: return listOf()
        return cmd.tabComplete(sender, args.drop(1).toTypedArray())
    }

    fun registerCmd(namespace: String) {
        register(namespace)
    }
}