package gg.aquatic.kurrency

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formats a BigDecimal currency value to a compact form with suffix (k, M, B, etc.)
 * Always shows 2 decimal places for values < 1000
 * Rounds DOWN to avoid displaying more money than actually exists
 *
 * @return Formatted string like "1.23k", "45.60M", etc.
 */
fun BigDecimal.formatBalanceWithSuffix(): String {
    // Always round DOWN for financial safety (never show more money than exists)
    val absValue = this.abs().setScale(2, RoundingMode.DOWN)

    // Define suffixes with their corresponding values
    val suffixes = arrayOf(
        "" to BigDecimal.ONE,
        "k" to BigDecimal("1000"),
        "M" to BigDecimal("1000000"),
        "B" to BigDecimal("1000000000"),
        "T" to BigDecimal("1000000000000"),
        "Q" to BigDecimal("1000000000000000"),
        "QQ" to BigDecimal("1000000000000000000"),
        "S" to BigDecimal("1000000000000000000000"),
        "SS" to BigDecimal("1000000000000000000000000"),
        "O" to BigDecimal("1000000000000000000000000000"),
        "N" to BigDecimal("1000000000000000000000000000000"),
        "D" to BigDecimal("1000000000000000000000000000000000"),
        "UD" to BigDecimal("1000000000000000000000000000000000000")
    )

    // Special case for zero
    if (this.compareTo(BigDecimal.ZERO) == 0) {
        return "0.00"
    }

    // Find the appropriate suffix
    var i = suffixes.size - 1
    while (i > 0) {
        if (absValue >= suffixes[i].second) {
            break
        }
        i--
    }

    val (suffix, divisor) = suffixes[i]

    // Format based on the value range
    return when {
        // For values less than 1000, always show 2 decimal places
        absValue < BigDecimal("1000") -> {
            val formatter = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US))
            formatter.roundingMode = RoundingMode.DOWN
            (if (this.signum() < 0) "-" else "") + formatter.format(absValue)
        }

        // For values 1k and higher, use 2 decimal places with suffix
        else -> {
            val scaled = this.divide(divisor, 4, RoundingMode.DOWN)
                .setScale(2, RoundingMode.DOWN)

            // Format with exactly 2 decimal places
            val formatter = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US))
            formatter.roundingMode = RoundingMode.DOWN
            val formattedNumber = formatter.format(scaled)

            formattedNumber + suffix
        }
    }
}

/**
 * Formats a BigDecimal for raw display with exactly 2 decimal places
 * Always rounds down (floor) for financial safety
 */
fun BigDecimal.formatRawBalance(): String {
    val formatter = DecimalFormat("###,###,###,###,##0.00", DecimalFormatSymbols(Locale.US))
    formatter.roundingMode = RoundingMode.DOWN
    return formatter.format(this.setScale(2, RoundingMode.DOWN))
}

/**
 * Formats a BigDecimal for raw display with variable decimal places, but only shows
 * decimal places if they're non-zero
 */
fun BigDecimal.formatCompactBalance(maxDecimalPlaces: Int = 2): String {
    val rounded = this.setScale(maxDecimalPlaces, RoundingMode.DOWN)

    // If it's a whole number, don't show decimal places
    return if (rounded.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
        rounded.setScale(0, RoundingMode.DOWN).toPlainString()
    } else {
        // Remove trailing zeros
        rounded.stripTrailingZeros().toPlainString()
    }
}

/**
 * Parses a string with a suffix (k, M, B, etc.) back to a BigDecimal
 * @param value String like "1.23k" or "45M"
 * @return BigDecimal value or null if parsing failed
 */
fun parseSuffixedBalance(value: String): BigDecimal? {
    val regex = """^([-+]?\d*\.?\d+)([kMBTQSOnNdU]*)$""".toRegex()
    val match = regex.find(value.trim()) ?: return null

    val (number, suffix) = match.destructured
    val numValue = number.toBigDecimalOrNull() ?: return null

    val multiplier = when (suffix.uppercase()) {
        "" -> BigDecimal.ONE
        "K" -> BigDecimal("1000")
        "M" -> BigDecimal("1000000")
        "B" -> BigDecimal("1000000000")
        "T" -> BigDecimal("1000000000000")
        "Q" -> BigDecimal("1000000000000000")
        "QQ" -> BigDecimal("1000000000000000000")
        "S" -> BigDecimal("1000000000000000000000")
        "SS" -> BigDecimal("1000000000000000000000000")
        "O" -> BigDecimal("1000000000000000000000000000")
        "N" -> BigDecimal("1000000000000000000000000000000")
        "D" -> BigDecimal("1000000000000000000000000000000000")
        "UD" -> BigDecimal("1000000000000000000000000000000000000")
        else -> return null
    }

    return numValue.multiply(multiplier)
}