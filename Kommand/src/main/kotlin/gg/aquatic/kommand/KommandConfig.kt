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

        val minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer")

        val server = getServer.invoke(bukkitServer)
        val resourcesField = minecraftServerClass.getDeclaredField("resources")
        resourcesField.isAccessible = true

        val resources = resourcesField.get(server)
        val getManagers = resources.javaClass.getDeclaredMethod("managers")
        getManagers.isAccessible = true

        val managers = getManagers.invoke(resources)
        val commandsField = managers.javaClass.getDeclaredField("commands")
        commandsField.isAccessible = true

        val commands = commandsField.get(managers)
        val getDispatcher = commands.javaClass.getDeclaredMethod("getDispatcher")
        getDispatcher.isAccessible = true

        getDispatcher.invoke(commands) as CommandDispatcher<CommandSourceStack>
    }
}