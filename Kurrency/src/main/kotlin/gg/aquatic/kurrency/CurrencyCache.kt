package gg.aquatic.kurrency

import gg.aquatic.kurrency.impl.VirtualCurrency
import java.math.BigDecimal
import java.util.*

interface CurrencyCache {

    suspend fun get(uuid: UUID, virtualCurrency: VirtualCurrency): BigDecimal
    suspend fun getMultiple(uuids: Collection<UUID>, virtualCurrency: VirtualCurrency): Map<UUID, BigDecimal>
    suspend fun put(uuid: UUID, amount: BigDecimal, virtualCurrency: VirtualCurrency)
    suspend fun invalidate(uuid: UUID, virtualCurrency: VirtualCurrency? = null)
}
