package gg.aquatic.waves.command

import org.bukkit.Bukkit
import org.bukkit.command.Command

fun Command.register(namespace: String) {
    unregister()
    Bukkit.getServer().commandMap.register(namespace, this)
}

fun Command.unregister() {
    val commandMap = Bukkit.getServer().commandMap
    val knownCommands = commandMap.knownCommands
    knownCommands.remove(name)
    for (alias in this.aliases) {
        if (knownCommands.containsKey(alias) && knownCommands[alias]!!.name.contains(this.name))
            knownCommands.remove(alias)
    }
}