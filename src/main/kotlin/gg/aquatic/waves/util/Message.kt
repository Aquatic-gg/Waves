package gg.aquatic.waves.util

import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.function.Consumer

class Message(var messages: Collection<String>) {

    constructor(message: String?): this(message?.let { mutableListOf(it) } ?: mutableListOf())

    fun replace(updater: (String) -> String): Message {
        this.messages = messages.map { updater(it) }.toMutableList()
        return this
    }

    fun replace(from: String, to: String): Message {
        messages = messages.map { it.replace(from, to) }.toMutableList()
        return this
    }

    fun send(player: Player) {
        if (messages.size == 1 && messages.first().isEmpty()) {
            return
        }
        val user = player.toUser() ?: return
        messages.forEach(Consumer { v: String ->
            user.sendMessage(v.toMMComponent())
        })
    }

    fun send(sender: CommandSender) {
        if (sender !is Player) {
            val msgs = messages.map { ComponentSerializer.parse(it.toMMComponent().toJson()) }
            for (baseComponents in msgs) {
                for (baseComponent in baseComponents) {
                    sender.spigot().sendMessage(baseComponent)
                }
            }
        } else {
            send(sender)
        }
    }

    fun broadcast() {
        if (messages.size == 1 && messages.first().isEmpty() || messages.isEmpty()) {
            return
        }
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            val user = onlinePlayer.toUser()
            messages.forEach(Consumer { v: String ->
                user?.sendMessage(v.toMMComponent())
            })
        }

    }

}