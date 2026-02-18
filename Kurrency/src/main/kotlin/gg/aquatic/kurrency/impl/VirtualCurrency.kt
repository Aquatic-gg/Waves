package gg.aquatic.kurrency.impl

import gg.aquatic.kurrency.KurrencyConfig
import java.math.BigDecimal
import java.util.*

class VirtualCurrency(
    val id: String,
    val prefix: String = "",
    val suffix: String = ""
) {

    suspend fun give(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        KurrencyConfig.currencyHandler.give(uuid, registeredCurrency, amount)
    }

    suspend fun take(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        KurrencyConfig.currencyHandler.give(uuid, registeredCurrency, amount.abs().negate())
    }

    suspend fun set(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        KurrencyConfig.currencyHandler.set(uuid, registeredCurrency, amount)
    }

    suspend fun getBalance(uuid: UUID, registeredCurrency: RegisteredCurrency): BigDecimal {
        return KurrencyConfig.currencyHandler.getBalance(uuid, registeredCurrency)
    }

    suspend fun tryTake(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency): Boolean {
        return KurrencyConfig.currencyHandler.tryTake(uuid, registeredCurrency, amount)
    }
}