package gg.aquatic.waves.util.message.parser

import gg.aquatic.waves.registry.serializer.ActionSerializer
import gg.aquatic.waves.util.getSectionList
import gg.aquatic.waves.util.message.Message
import gg.aquatic.waves.util.message.impl.SimpleMessage
import gg.aquatic.waves.util.message.parser.click.ClickAction
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

object MessageParser {

    fun parse(section: ConfigurationSection): Message {
        val actions = ActionSerializer.fromSections<Player>(section.getSectionList("actions"))
        val messageSections = section.getSectionList("messages") + section.getSectionList("message")
        val messages = messageSections.map { parseMessage(it) }.toList()
        return SimpleMessage(messages, actions)
    }

    private fun parseMessage(section: ConfigurationSection): String {
        val componentSections = section.getSectionList("components")
        val components = componentSections.mapNotNull { parseComponent(it) }
        return components.joinToString("<reset>")
    }

    private fun parseComponent(section: ConfigurationSection): String? {
        var text = section.getString("text") ?: return null
        val clickAction = section.getConfigurationSection("click")?.let {
            ClickAction.load(it)
        }
        val hover = section.getStringList("hover")
        text = clickAction?.bind(text) ?: text
        if (hover.isNotEmpty()) {
            text = "<hover:show_text:'${hover.joinToString("<nl>")}'>$text</hover>"
        }
        return text
    }

}