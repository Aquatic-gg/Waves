package gg.aquatic.waves.util.message.impl.page

import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import org.bukkit.Bukkit

object ConsoleCommandMMResolver {
    fun resolve(args: ArgumentQueue, ctx: Context): Tag {
        val command = args.pop().value()

        return Tag.styling {
            ClickEvent.callback {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
            }
        }
    }
}