package gg.aquatic.kurrency.impl

import gg.aquatic.kregistry.Registry
import gg.aquatic.kurrency.KurrencyConfig
import gg.aquatic.kurrency.impl.RegisteredCurrency.Companion.REGISTRY_KEY
import java.math.BigDecimal
import java.util.*

class VirtualCurrency(
    val id: String,
    val prefix: String = "",
    val suffix: String = ""
) {

    fun register(): RegisteredCurrency {
        val registry = RegisteredCurrency(this)

        Registry.update {
            replaceRegistry(REGISTRY_KEY) {
                this.register(id, registry)
            }
        }

        return registry
    }

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