package gg.aquatic.waves.util

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.player.User
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

fun Player.toUser(): User? {
    return PacketEvents.getAPI().playerManager.getUser(this)
}

fun Player.sendMessage(vararg components: Component) {
    val user = toUser() ?: return
    for (component in components) {
        user.sendMessage(component)
    }
}