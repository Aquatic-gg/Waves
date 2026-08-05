package gg.aquatic.kmenu

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MenuSerializerTest {

    @Test
    fun `test slot string parsing logic`() {
        val input = listOf("0-2", "9", "11-11")

        // This mirrors the logic inside MenuSerializer.loadSlotSelection
        val parsed = input.flatMap { slot ->
            if (slot.contains("-")) {
                val range = slot.split("-")
                val start = range[0].toIntOrNull() ?: 0
                val end = range[1].toIntOrNull() ?: 0
                (start..end).toList()
            } else {
                listOfNotNull(slot.toIntOrNull())
            }
        }.toSet()

        assertEquals(setOf(0, 1, 2, 9, 11), parsed)
    }
}
