package gg.aquatic.kurrency

import gg.aquatic.kurrency.impl.RegisteredCurrency
import java.math.BigDecimal
import java.util.*

interface CurrencyCache {

    suspend fun get(uuid: UUID, registeredCurrency: RegisteredCurrency): BigDecimal?
    suspend fun getMultiple(uuids: Collection<UUID>, registeredCurrency: RegisteredCurrency): Map<UUID, BigDecimal>
    suspend fun update(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency)
    suspend fun set(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency)

    suspend fun isActive(uuid: UUID): Boolean
}