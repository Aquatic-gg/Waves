package gg.aquatic.kurrency.db

import gg.aquatic.common.coroutine.VirtualsCtx
import gg.aquatic.kurrency.impl.RegisteredCurrency
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.upsert
import java.math.BigDecimal
import java.util.*

class CurrencyDBHandler(val database: Database) {

    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(db = database) { block() }

    suspend fun getBalance(uuid: UUID, currency: RegisteredCurrency): BigDecimal = withContext(VirtualsCtx) {
        dbQuery {
            BalancesTable.select(BalancesTable.balance)
                .where { (BalancesTable.playerUUID eq uuid) and (BalancesTable.currencyId eq currency.id) }
                .singleOrNull()?.get(BalancesTable.balance) ?: BigDecimal.ZERO
        }
    }

    suspend fun getAllBalances(uuid: UUID): Map<String, BigDecimal> = withContext(VirtualsCtx) {
        dbQuery {
            BalancesTable.select(BalancesTable.currencyId, BalancesTable.balance)
                .where { BalancesTable.playerUUID eq uuid }
                .associate { it[BalancesTable.currencyId] to it[BalancesTable.balance] }
        }
    }

    suspend fun getBalances(uuids: Collection<UUID>, currency: RegisteredCurrency): Map<UUID, BigDecimal> = withContext(VirtualsCtx) {
        dbQuery {
            BalancesTable.select(BalancesTable.playerUUID, BalancesTable.balance)
                .where { (BalancesTable.playerUUID inList uuids) and (BalancesTable.currencyId eq currency.id) }
                .associate { it[BalancesTable.playerUUID] to it[BalancesTable.balance] }
        }
    }

    suspend fun give(uuid: UUID, amount: BigDecimal, currency: RegisteredCurrency) = withContext(VirtualsCtx) {
        dbQuery {
            val result = BalancesTable.upsert(
                onUpdate = {
                    it[BalancesTable.balance] = BalancesTable.balance + amount
                }
            ) {
                it[BalancesTable.playerUUID] = uuid
                it[BalancesTable.currencyId] = currency.id
                it[BalancesTable.balance] = amount
            }

            if (result.insertedCount == 0) {
                throw IllegalStateException("Database update failed for $uuid (Zero rows affected)")
            }
        }
    }

    suspend fun set(uuid: UUID, amount: BigDecimal, currency: RegisteredCurrency) = withContext(VirtualsCtx) {
        dbQuery {
            val result = BalancesTable.upsert {
                it[BalancesTable.playerUUID] = uuid
                it[BalancesTable.currencyId] = currency.id
                it[BalancesTable.balance] = amount
            }

            if (result.insertedCount == 0) {
                throw IllegalStateException("Database set failed for $uuid")
            }
        }
    }
}