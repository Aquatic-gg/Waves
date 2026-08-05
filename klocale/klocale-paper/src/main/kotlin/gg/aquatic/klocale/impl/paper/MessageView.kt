package gg.aquatic.klocale.impl.paper

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import java.time.Duration

interface MessageView {

    fun send(sender: CommandSender, components: Collection<Component>)

    interface Serializer {
        fun load(section: ConfigurationSection): MessageView?
    }

    companion object {
        val serializers = mutableMapOf<String, Serializer>(
            "chat" to object: Serializer {
                override fun load(section: ConfigurationSection): MessageView {
                    return Chat
                }
            },
            "action-bar" to object: Serializer {
                override fun load(section: ConfigurationSection): MessageView {
                    return ActionBar
                }
            },
            "title" to Title.Companion
        )

        fun load(section: ConfigurationSection): MessageView {
            val type = section.getString("view")?.lowercase() ?: return Chat
            val serializer = serializers[type] ?: return Chat
            return serializer.load(section) ?: Chat
        }
    }

    object Chat : MessageView {
        override fun send(sender: CommandSender, components: Collection<Component>) {
            components.forEach {
                sender.sendMessage(it)
            }
        }
    }

    object ActionBar : MessageView {
        override fun send(sender: CommandSender, components: Collection<Component>) {
            sender.sendActionBar(components.firstOrNull() ?: return)
        }
    }

    class Title(
        val fadeIn: Int = 20,
        val stay: Int = 60,
        val fadeOut: Int = 20,
    ) : MessageView {
        override fun send(sender: CommandSender, components: Collection<Component>) {
            sender.showTitle(
                net.kyori.adventure.title.Title.title(
                    components.firstOrNull() ?: return,
                    components.elementAtOrNull(1) ?: Component.empty(),
                    net.kyori.adventure.title.Title.Times.times(
                        Duration.ofMillis(fadeIn.toLong() * 50),
                        Duration.ofMillis(stay.toLong() * 50),
                        Duration.ofMillis(fadeOut.toLong() * 50)
                    )
                )
            )
        }
        companion object: Serializer {
            override fun load(section: ConfigurationSection): MessageView {
                val fadeIn = section.getInt("fade-in")
                val stay = section.getInt("stay")
                val fadeOut = section.getInt("fade-out")
                return Title(fadeIn, stay, fadeOut)
            }

        }
    }
}