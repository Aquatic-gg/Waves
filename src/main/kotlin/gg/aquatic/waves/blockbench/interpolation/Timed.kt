package gg.aquatic.waves.blockbench.interpolation

interface Timed: Comparable<Timed> {

    val time: Float

    override fun compareTo(other: Timed): Int {
        return time.compareTo(other.time)
    }

}