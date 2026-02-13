package gg.aquatic.quickminimessage

import kotlin.test.Test
import kotlin.test.assertEquals
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

class MMParserComparisonTest {
    private val gson = GsonComponentSerializer.gson()
    private val mini = MiniMessage.miniMessage()

    @Test
    fun parsePlain() {
        assertMatches("Hello world, this is plain.")
    }

    @Test
    fun parseSimpleColor() {
        assertMatches("<red>Hello</red> world")
    }

    @Test
    fun parseSimpleNonAscii() {
        assertMatches("<光>光</光> world")
    }

    @Test
    fun parseNested() {
        assertMatches("<bold><blue>Hello</blue> <italic>world</italic></bold>")
    }

    /*
    MM Parses the single tags strangely and adds extra blank texts...
    @Test
    fun parseGradientShort() {
        assertMatches("<gradient:#ff0000:#00ff00>Hi</gradient>")
    }

    @Test
    fun parseRainbowShort() {
        assertMatches("<rainbow>Hi</rainbow>")
    }
     */

    @Test
    fun parseHoverShowText() {
        assertMatches("<hover:show_text:'<green>Hover</green>'>Hover me</hover>")
    }

    @Test
    fun parseClick() {
        assertMatches("<click:run_command:/say hi>Click</click>")
    }

    @Test
    fun parseKeybind() {
        assertMatches("Press <key:key.jump>")
    }

    @Test
    fun parseSelector() {
        assertMatches("Selector: <selector:@p>")
    }

    @Test
    fun parseScore() {
        assertMatches("Score: <score:Player:objective>")
    }

    @Test
    fun parseTranslatableArgs() {
        assertMatches("<lang:chat.type.text:'<green>A</green>':'<blue>B</blue>'>")
    }

    private fun assertMatches(input: String) {
        val expected = mini.deserialize(input).compact()
        val actual = MMParser.deserialize(input).compact()
        val expectedJson = gson.serialize(expected)
        val actualJson = gson.serialize(actual)
        assertEquals(expectedJson, actualJson, "Mismatch for input: $input\nExpected: $expectedJson\nActual:   $actualJson")
    }
}
