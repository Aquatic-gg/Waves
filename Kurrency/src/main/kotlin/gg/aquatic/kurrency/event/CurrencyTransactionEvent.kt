package gg.aquatic.kurrency.event

import gg.aquatic.kurrency.impl.RegisteredCurrency
import java.math.BigDecimal
import java.util.*

data class CurrencyTransactionEvent(
    val uuid: UUID,
    val currency: RegisteredCurrency,
    val oldBalance: BigDecimal,
    val newBalance: BigDecimal,
    val change: BigDecimal
)
