package gg.aquatic.kmenu

import gg.aquatic.kmenu.menu.SlotSelection
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SlotSelectionTest {

    @Test
    fun `test rect calculation for 2x2 square`() {
        // Top-left 2x2 square: (0,0), (1,0), (0,1), (1,1) -> 0, 1, 9, 10
        val selection = SlotSelection.rect(0, 10)
        val expected = setOf(0, 1, 9, 10)
        assertEquals(expected, selection.slots)
    }

    @Test
    fun `test rect calculation across rows`() {
        // Vertical line in column 1: slots 1, 10, 19
        val selection = SlotSelection.rect(1, 19)
        val expected = setOf(1, 10, 19)
        assertEquals(expected, selection.slots)
    }

    @Test
    fun `test andRange combination`() {
        val selection = SlotSelection.of(0).andRange(1, 2)
        assertTrue(selection.slots.containsAll(listOf(0, 1, 2)))
        assertEquals(3, selection.slots.size)
    }
}
