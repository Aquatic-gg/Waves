package gg.aquatic.kurrency.impl

import gg.aquatic.kurrency.Kurrency
import java.math.BigDecimal
import java.util.*

class VirtualCurrency(
    val id: String,
    val prefix: String = "",
    val suffix: String = ""
) {

    suspend fun give(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        Kurrency.currencyHandler.give(uuid, registeredCurrency, amount)
    }

    suspend fun take(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        Kurrency.currencyHandler.take(uuid, registeredCurrency, amount)
    }

    suspend fun set(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        Kurrency.currencyHandler.set(uuid, registeredCurrency, amount)
    }

    suspend fun getBalance(uuid: UUID, registeredCurrency: RegisteredCurrency): BigDecimal {
        return Kurrency.currencyHandler.getBalance(uuid, registeredCurrency)
    }

    suspend fun tryTake(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency): Boolean {
        return Kurrency.currencyHandler.tryTake(uuid, registeredCurrency, amount)
    }
}
