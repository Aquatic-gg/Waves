package gg.aquatic.kurrency.db

import gg.aquatic.common.coroutine.VirtualsCtx
import gg.aquatic.kurrency.impl.RegisteredCurrency
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.upsert
import org.jetbrains.exposed.v1.core.*
import java.math.BigDecimal
import java.util.*

class CurrencyDBHandler(val database: Database) {

    private suspend fun <T> dbQuery(block: suspend JdbcTransaction.() -> T): T =
        suspendTransaction(db = database) { block() }

    private fun JdbcTransaction.getBalanceInternal(uuid: UUID, currency: RegisteredCurrency): BigDecimal {
        return BalancesTable.select(BalancesTable.balance)
            .where { (BalancesTable.playerUUID eq uuid) and (BalancesTable.currencyId eq currency.id) }
            .singleOrNull()?.get(BalancesTable.balance) ?: BigDecimal.ZERO
    }

    private fun JdbcTransaction.storeBalance(uuid: UUID, amount: BigDecimal, currency: RegisteredCurrency) {
        val result = BalancesTable.upsert {
            it[BalancesTable.playerUUID] = uuid
            it[BalancesTable.currencyId] = currency.id
            it[BalancesTable.balance] = amount
        }

        if (result.insertedCount == 0) {
            throw IllegalStateException("Database set failed for $uuid")
        }
    }

    suspend fun getBalance(uuid: UUID, currency: RegisteredCurrency): BigDecimal = withContext(VirtualsCtx) {
        dbQuery { getBalanceInternal(uuid, currency) }
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

    suspend fun changeBy(uuid: UUID, amount: BigDecimal, currency: RegisteredCurrency): BigDecimal = withContext(VirtualsCtx) {
        dbQuery {
            val newBalance = getBalanceInternal(uuid, currency).add(amount)
            storeBalance(uuid, newBalance, currency)
            newBalance
        }
    }

    suspend fun setAndGet(uuid: UUID, amount: BigDecimal, currency: RegisteredCurrency): BigDecimal = withContext(VirtualsCtx) {
        dbQuery {
            storeBalance(uuid, amount, currency)
            amount
        }
    }

    suspend fun tryTake(uuid: UUID, amount: BigDecimal, currency: RegisteredCurrency): BigDecimal? = withContext(VirtualsCtx) {
        dbQuery {
            val current = getBalanceInternal(uuid, currency)
            if (current < amount) {
                return@dbQuery null
            }

            val newBalance = current.subtract(amount)
            if (newBalance < BigDecimal.ZERO) {
                return@dbQuery null
            }

            storeBalance(uuid, newBalance, currency)
            newBalance
        }
    }

    suspend fun getLeaderboard(currency: RegisteredCurrency, limit: Int = 10): List<Pair<UUID, BigDecimal>> = withContext(VirtualsCtx) {
        dbQuery {
            BalancesTable.select(BalancesTable.playerUUID, BalancesTable.balance)
                .where { BalancesTable.currencyId eq currency.id }
                .orderBy(BalancesTable.balance to SortOrder.DESC)
                .limit(limit)
                .map { it[BalancesTable.playerUUID] to it[BalancesTable.balance] }
        }
    }

    suspend fun getPlayerRank(uuid: UUID, currency: RegisteredCurrency): Long = withContext(VirtualsCtx) {
        dbQuery {
            val playerBalance = BalancesTable.select(BalancesTable.balance)
                .where { (BalancesTable.playerUUID eq uuid) and (BalancesTable.currencyId eq currency.id) }
                .singleOrNull()?.get(BalancesTable.balance) ?: return@dbQuery -1L

            // Rank is count of players with higher balance + 1
            BalancesTable.select(BalancesTable.playerUUID)
                .where { (BalancesTable.currencyId eq currency.id) and (BalancesTable.balance greater playerBalance) }
                .count() + 1
        }
    }
}
