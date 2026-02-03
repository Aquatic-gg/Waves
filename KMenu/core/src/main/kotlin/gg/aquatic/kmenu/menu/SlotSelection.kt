package gg.aquatic.kmenu.menu

/** Utility for grouping slot indices for menu layout. */
class SlotSelection(slots: Collection<Int>) {

    // Using a HashSet for O(1) lookups
    val slots: Set<Int> = slots.toHashSet()

    companion object {
        fun of(vararg slots: Int): SlotSelection {
            return SlotSelection(slots.toList())
        }

        fun of(collection: Collection<Int>): SlotSelection {
            return SlotSelection(collection)
        }

        fun rangeOf(from: Int, to: Int): SlotSelection {
            return SlotSelection((from..to).toList())
        }

        fun rect(topLeft: Int, bottomRight: Int): SlotSelection {
            if (topLeft == bottomRight) return of(topLeft)

            val x1 = topLeft % 9
            val y1 = topLeft / 9
            val x2 = bottomRight % 9
            val y2 = bottomRight / 9

            val minX = minOf(x1, x2)
            val maxX = maxOf(x1, x2)
            val minY = minOf(y1, y2)
            val maxY = maxOf(y1, y2)

            val set = mutableListOf<Int>()
            for (y in minY..maxY) {
                for (x in minX..maxX) {
                    set.add(y * 9 + x)
                }
            }
            return SlotSelection(set)
        }
    }

    fun containsSlot(slot: Int): Boolean = slots.contains(slot)

    fun getSorted(): Set<Int> = slots.toSortedSet()

    fun and(vararg slots: Int): SlotSelection = SlotSelection(this.slots + slots.toList())

    fun andRange(from: Int, to: Int): SlotSelection = SlotSelection(this.slots + (from..to).toList())

    fun andRect(topLeft: Int, bottomRight: Int): SlotSelection {
        return SlotSelection(this.slots + rect(topLeft, bottomRight).slots)
    }

}
