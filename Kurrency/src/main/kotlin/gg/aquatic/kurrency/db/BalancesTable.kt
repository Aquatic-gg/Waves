package gg.aquatic.kurrency.db

import org.jetbrains.exposed.sql.Table

object BalancesTable: Table("currency_balances") {
    val playerUUID = uuid("player_uuid")
    val currencyId = varchar("currency_id", 36)
    val balance = decimal("balance", precision = 32, scale = 2)
}