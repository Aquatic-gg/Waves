package gg.aquatic.quickminimessage.parser

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.util.HSVLike

internal interface Colorizer {
    fun init(size: Int)
    fun color(): TextColor
    fun advance()
}

internal class RainbowColorizer(
    private val reversed: Boolean,
    phase: Int
) : Colorizer {
    companion object {
        private const val CACHE_BITS = 8 // 256 entries
        private const val CACHE_SIZE = 1 shl CACHE_BITS
        private const val CACHE_MASK = CACHE_SIZE - 1

        private const val PRECISION = 16

        private val COLOR_CACHE = IntArray(CACHE_SIZE) { i ->
            val hsv = HSVLike.hsvLike(i.toFloat() / CACHE_SIZE, 1f, 1f)
            TextColor.color(hsv).value()
        }
    }

    private val phaseOffset = ((phase / 10.0) % 1.0 * CACHE_SIZE).toInt() shl PRECISION
    private var currentPos = 0
    private var step = 0

    override fun init(size: Int) {
        if (size <= 0) {
            step = 0
            currentPos = phaseOffset
            return
        }

        val totalDist = CACHE_SIZE shl PRECISION
        step = totalDist / size

        if (reversed) {
            step = -step
            currentPos = phaseOffset + (totalDist + step)
        } else {
            currentPos = phaseOffset
        }
    }

    override fun color(): TextColor {
        val cacheIndex = (currentPos shr PRECISION) and CACHE_MASK
        return TextColor.color(COLOR_CACHE[cacheIndex])
    }

    override fun advance() {
        currentPos += step
    }
}

internal class GradientColorizer(colors: Array<TextColor>, phase: Double) : Colorizer {
    companion object {
        private const val PRECISION = 16
        private const val SCALE = 1 shl PRECISION
        private const val MASK = SCALE - 1
    }

    private val colors: IntArray
    private val phaseOffset: Int
    private var currentPos = 0
    private var step = 0

    init {
        val actualColors = if (phase < 0) colors.reversedArray() else colors
        this.colors = IntArray(actualColors.size) { actualColors[it].value() }
        val normalizedPhase = if (phase < 0) 1.0 + phase else phase
        this.phaseOffset = (normalizedPhase * (actualColors.size - 1) * SCALE).toInt()
    }

    override fun init(size: Int) {
        if (size <= 1) {
            step = 0
            currentPos = phaseOffset
            return
        }

        val totalDist = (colors.size - 1) shl PRECISION
        step = totalDist / (size - 1)
        currentPos = phaseOffset
    }

    override fun color(): TextColor {
        val pos = currentPos
        val maxPos = (colors.size - 1) shl PRECISION

        val clampedPos = pos.coerceIn(0, maxPos)

        val index = clampedPos shr PRECISION
        val fraction = clampedPos and MASK

        if (index >= colors.size - 1) {
            return TextColor.color(colors.last())
        }

        val c1 = colors[index]
        val c2 = colors[index + 1]

        val r = (((c1 shr 16 and 0xFF) * (SCALE - fraction)) + ((c2 shr 16 and 0xFF) * fraction)) shr PRECISION
        val g = (((c1 shr 8 and 0xFF) * (SCALE - fraction)) + ((c2 shr 8 and 0xFF) * fraction)) shr PRECISION
        val b = (((c1 and 0xFF) * (SCALE - fraction)) + ((c2 and 0xFF) * fraction)) shr PRECISION

        return TextColor.color((r shl 16) or (g shl 8) or b)
    }

    override fun advance() {
        currentPos += step
    }
}

