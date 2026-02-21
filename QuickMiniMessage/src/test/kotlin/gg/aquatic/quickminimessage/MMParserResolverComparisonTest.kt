package gg.aquatic.quickminimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

class MMParserResolverComparisonTest {
    private val gson = GsonComponentSerializer.gson()
    private val mini = MiniMessage.miniMessage()

    @Test
    fun placeholderParsed() {
        val input = "Hello <name>!"
        val expected = mini.deserialize(input, Placeholder.parsed("name", "<red>Bob</red>")).compact()
        val actual = MMParser.deserialize(input, MMPlaceholder.parsed("name", "<red>Bob</red>")).compact()
        assertEquals(gson.serialize(expected), gson.serialize(actual))
    }

    @Test
    fun placeholderUnparsed() {
        val input = "Hello <name>!"
        val expected = mini.deserialize(input, Placeholder.unparsed("name", "<red>Bob</red>")).compact()
        val actual = MMParser.deserialize(input, MMPlaceholder.unparsed("name", "<red>Bob</red>")).compact()
        assertEquals(gson.serialize(expected), gson.serialize(actual))
    }

    @Test
    fun placeholderStyling() {
        val input = "<fancy>Hello</fancy> world"
        val expected = mini.deserialize(
            input,
            Placeholder.styling("fancy", NamedTextColor.RED, TextDecoration.BOLD)
        ).compact()
        val actual = MMParser.deserialize(
            input,
            MMPlaceholder.styling("fancy", NamedTextColor.RED, TextDecoration.BOLD)
        ).compact()
        assertEquals(gson.serialize(expected), gson.serialize(actual))
    }

    @Test
    fun formatterNumber() {
        val input = "Balance: <no:'en-US':'#.00'>"
        val expected = mini.deserialize(input, Formatter.number("no", 250.25)).compact()
        val actual = MMParser.deserialize(input, MMFormatter.number("no", 250.25)).compact()
        assertEquals(gson.serialize(expected), gson.serialize(actual))
    }

    @Test
    fun formatterJoining() {
        val input = "Items: <items:'<gray>, </gray>':'<gray> and </gray>'>"
        val expected = mini.deserialize(
            input,
            Formatter.joining("items", Component.text("A"), Component.text("B"), Component.text("C"))
        ).compact()
        val actual = MMParser.deserialize(
            input,
            MMFormatter.joining("items", Component.text("A"), Component.text("B"), Component.text("C"))
        ).compact()
        assertEquals(gson.serialize(expected), gson.serialize(actual))
    }
}
