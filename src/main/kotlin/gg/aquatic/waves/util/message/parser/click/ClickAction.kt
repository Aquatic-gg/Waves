package gg.aquatic.waves.util.message.parser.click

import org.bukkit.configuration.ConfigurationSection

interface ClickAction {

    companion object {
        private val serializers = mutableMapOf<String, Serializer>(
            "open-url" to OpenUrl,
            "run-command" to RunCommand,
            "suggest-command" to SuggestCommand,
            "copy-to-clipboard" to CopyToClipboard,
        )
        fun load(section: ConfigurationSection): ClickAction? {
            val type = section.getString("type")?.lowercase() ?: return null
            val serializer = serializers[type] ?: return null
            return serializer.load(section)
        }
    }

    fun bind(component: String): String

    interface Serializer {
        fun load(section: ConfigurationSection): ClickAction
    }

    class OpenUrl(val url: String) : ClickAction {
        override fun bind(component: String): String {
            return "<click:open_url:'$url'>$component</click>"
        }

        companion object : Serializer {
            override fun load(section: ConfigurationSection): ClickAction {
                return OpenUrl(section.getString("url") ?: "")
            }
        }
    }

    class RunCommand(val command: String) : ClickAction {
        override fun bind(component: String): String {
            return "<click:run_command:'$command'>$component</click>"
        }

        companion object : Serializer {
            override fun load(section: ConfigurationSection): ClickAction {
                return RunCommand(section.getString("command") ?: "")
            }
        }
    }

    class ConsoleCommand(val command: String) : ClickAction {
        override fun bind(component: String): String {
            return "<click:ccmd:'$command'>$component</click>"
        }
    }

    class SuggestCommand(val command: String) : ClickAction {
        override fun bind(component: String): String {
            return "<click:suggest_command:'$command'>$component</click>"
        }

        companion object : Serializer {
            override fun load(section: ConfigurationSection): ClickAction {
                return SuggestCommand(section.getString("command") ?: "")
            }
        }
    }

    class CopyToClipboard(val text: String) : ClickAction {
        override fun bind(component: String): String {
            return "<click:copy_to_clipboard:'$text'>$component</click>"
        }

        companion object : Serializer {
            override fun load(section: ConfigurationSection): ClickAction {
                return CopyToClipboard(section.getString("text") ?: "")
            }
        }
    }
}