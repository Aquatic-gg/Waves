package gg.aquatic.waves.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun Component.broadcast() {
    Bukkit.broadcast(this)
}

fun Component.toMMString(): String {
    return MiniMessage.miniMessage().serialize(this)
}

fun Component.toJson(): String {
    return net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(this)
}

fun Component.toPlain(): String {
    return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(this)
}

fun Component.send(sender: CommandSender) {
    sender.sendMessage(this)
}

fun Component.send(player: Player) {
    player.sendMessage(this)
}

fun CommandSender.send(component: Component) {
    component.send(this)
}