package gg.aquatic.treepapi

import kotlin.test.Test
import kotlin.test.assertEquals

class PAPIParserTest {

    @Test
    fun `test standard splitting`() {
        val input = "stats_kills_total"
        val expected = listOf("stats", "kills", "total")
        assertEquals(expected, PAPIParser.parse(input))
    }

    @Test
    fun `test quoted argument with underscores`() {
        val input = "display_\"text_with_underscores\"_suffix"
        val expected = listOf("display", "text_with_underscores", "suffix")
        assertEquals(expected, PAPIParser.parse(input))
    }

    @Test
    fun `test empty input`() {
        assertEquals(emptyList(), PAPIParser.parse(""))
    }
}
