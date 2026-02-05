package gg.aquatic.quickminimessage.parser

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.util.HSVLike
import kotlin.math.ceil
import kotlin.math.floor
internal interface Colorizer {
    fun init(size: Int)
    fun color(): TextColor
    fun advance()
}

internal class RainbowColorizer(
    private val reversed: Boolean,
    phase: Int
) : Colorizer {
    private var size = 0
    private var index = 0
    private val dividedPhase = phase / 10.0

    override fun init(size: Int) {
        this.size = size
        this.index = if (reversed) size - 1 else 0
    }

    override fun color(): TextColor {
        if (size <= 0) {
            return TextColor.color(0xffffff)
        }
        val hue = ((index.toDouble() / size) + dividedPhase) % 1.0
        return TextColor.color(HSVLike.hsvLike(hue.toFloat(), 1f, 1f))
    }

    override fun advance() {
        if (size <= 0) {
            return
        }
        if (reversed) {
            index = if (index == 0) size - 1 else index - 1
        } else {
            index++
        }
    }
}

internal class GradientColorizer(colors: Array<TextColor>, phase: Double) : Colorizer {
    private val colors: Array<TextColor>
    private var phase = 0.0
    private var multiplier = 1.0
    private var index = 0

    init {
        if (phase < 0) {
            this.colors = colors.reversedArray()
            this.phase = 1 + phase
        } else {
            this.colors = colors
            this.phase = phase
        }
    }

    override fun init(size: Int) {
        multiplier = if (size <= 1) 0.0 else (colors.size - 1).toDouble() / (size - 1)
        phase *= colors.size - 1
        index = 0
    }

    override fun color(): TextColor {
        val position = (index * multiplier) + phase
        val lowUnclamped = floor(position).toInt()
        val high = ceil(position).toInt() % colors.size
        val low = lowUnclamped % colors.size
        return TextColor.lerp((position - lowUnclamped).toFloat(), colors[low], colors[high])
    }

    override fun advance() {
        index++
    }
}

