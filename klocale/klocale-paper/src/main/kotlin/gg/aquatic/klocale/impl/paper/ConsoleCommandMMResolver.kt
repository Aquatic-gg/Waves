package gg.aquatic.klocale.impl.paper

import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import org.bukkit.Bukkit

object ConsoleCommandMMResolver {
    fun resolve(args: ArgumentQueue, ctx: Context): Tag {
        val command = args.pop().value()

        return Tag.styling {
            it.clickEvent(ClickEvent.callback {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
            })
        }
    }
}