package gg.aquatic.kommand

import com.mojang.brigadier.CommandDispatcher
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.bukkit.Bukkit

object KommandConfig {

    var commandScope = CoroutineScope(Dispatchers.Default)

    @Suppress("UNCHECKED_CAST")
    val commandDispatcher by lazy {
        val bukkitServer = Bukkit.getServer()
        val getServer = bukkitServer.javaClass.getDeclaredMethod("getServer")
        getServer.isAccessible = true

        val server = getServer.invoke(bukkitServer)
        val getCommands = server.javaClass.getMethod("getCommands")
        getCommands.isAccessible = true

        val commands = getCommands.invoke(server)
        val getDispatcher = commands.javaClass.getMethod("getDispatcher")
        getDispatcher.isAccessible = true

        getDispatcher.invoke(commands) as CommandDispatcher<CommandSourceStack>
    }
}
