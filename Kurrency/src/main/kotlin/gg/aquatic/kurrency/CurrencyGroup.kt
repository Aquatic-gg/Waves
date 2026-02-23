package gg.aquatic.kurrency

import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.LinkedHashMap

class CurrencyGroup private constructor(
    private val requiredAmounts: LinkedHashMap<Currency, BigDecimal>
) {

    suspend fun tryTake(player: Player): Boolean {
        if (requiredAmounts.isEmpty()) return true

        val taken = ArrayList<Pair<Currency, BigDecimal>>(requiredAmounts.size)

        for ((currency, amount) in requiredAmounts) {
            if (amount == BigDecimal.ZERO) continue

            if (!currency.tryTake(player, amount)) {
                for (index in taken.indices.reversed()) {
                    val (takenCurrency, takenAmount) = taken[index]
                    takenCurrency.give(player, takenAmount)
                }
                return false
            }

            taken += currency to amount
        }

        return true
    }

    fun asMap(): Map<Currency, BigDecimal> = requiredAmounts

    companion object {
        fun of(requiredAmounts: Map<Currency, BigDecimal>): CurrencyGroup {
            val normalized = LinkedHashMap<Currency, BigDecimal>(requiredAmounts.size)
            requiredAmounts.forEach { (currency, amount) ->
                require(amount >= BigDecimal.ZERO) { "Amount to take must be positive." }
                normalized[currency] = amount
            }
            return CurrencyGroup(normalized)
        }

        fun of(vararg requiredAmounts: Pair<Currency, BigDecimal>): CurrencyGroup {
            val normalized = LinkedHashMap<Currency, BigDecimal>(requiredAmounts.size)
            requiredAmounts.forEach { (currency, amount) ->
                require(amount >= BigDecimal.ZERO) { "Amount to take must be positive." }
                val current = normalized[currency] ?: BigDecimal.ZERO
                normalized[currency] = current.add(amount)
            }
            return CurrencyGroup(normalized)
        }
    }
}
