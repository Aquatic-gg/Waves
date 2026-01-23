package gg.aquatic.kurrency.db

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

object BalancesTable: Table("currency_balances") {
    val playerUUID = javaUUID("player_uuid")
    val currencyId = varchar("currency_id", 36)
    val balance = decimal("balance", precision = 32, scale = 2)
}