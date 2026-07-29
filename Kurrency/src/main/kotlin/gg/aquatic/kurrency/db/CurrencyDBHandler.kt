package gg.aquatic.kurrency.db

import gg.aquatic.common.coroutine.VirtualsCtx
import gg.aquatic.kurrency.impl.VirtualCurrency
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.MinusOp
import org.jetbrains.exposed.v1.core.PlusOp
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.wrap
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.updateReturning
import org.jetbrains.exposed.v1.jdbc.upsert
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import java.math.BigDecimal
import java.util.UUID

class CurrencyDBHandler(val database: Database) {

    private suspend fun <T> dbQuery(block: suspend JdbcTransaction.() -> T): T =
        suspendTransaction(db = database) { block() }

    private fun JdbcTransaction.getBalanceInternal(uuid: UUID, currency: VirtualCurrency): BigDecimal {
        return BalancesTable.select(BalancesTable.balance)
            .where { (BalancesTable.playerUUID eq uuid) and (BalancesTable.currencyId eq currency.id) }
            .singleOrNull()?.get(BalancesTable.balance) ?: BigDecimal.ZERO
    }

    private fun JdbcTransaction.storeBalance(uuid: UUID, amount: BigDecimal, currency: VirtualCurrency) {
        val result = BalancesTable.upsert {
            it[BalancesTable.playerUUID] = uuid
            it[BalancesTable.currencyId] = currency.id
            it[BalancesTable.balance] = amount
        }

        if (result.insertedCount == 0) {
            throw IllegalStateException("Database set failed for $uuid")
        }
    }

    suspend fun getBalance(uuid: UUID, currency: VirtualCurrency): BigDecimal = withContext(VirtualsCtx) {
        dbQuery { getBalanceInternal(uuid, currency) }
    }

    suspend fun getAllBalances(uuid: UUID): Map<String, BigDecimal> = withContext(VirtualsCtx) {
        dbQuery {
            BalancesTable.select(BalancesTable.currencyId, BalancesTable.balance)
                .where { BalancesTable.playerUUID eq uuid }
                .associate { it[BalancesTable.currencyId] to it[BalancesTable.balance] }
        }
    }

    suspend fun getBalances(uuids: Collection<UUID>, currency: VirtualCurrency): Map<UUID, BigDecimal> =
        withContext(VirtualsCtx) {
            dbQuery {
                BalancesTable.select(BalancesTable.playerUUID, BalancesTable.balance)
                    .where { (BalancesTable.playerUUID inList uuids) and (BalancesTable.currencyId eq currency.id) }
                    .associate { it[BalancesTable.playerUUID] to it[BalancesTable.balance] }
            }
        }

    suspend fun changeBy(uuid: UUID, amount: BigDecimal, currency: VirtualCurrency): BigDecimal = withContext(VirtualsCtx) {
        dbQuery {
            BalancesTable.upsertReturning(
                returning = listOf(BalancesTable.balance),
                onUpdate = {
                    it[BalancesTable.balance] = PlusOp<BigDecimal, BigDecimal>(
                        BalancesTable.balance,
                        BalancesTable.balance.wrap(amount),
                        BalancesTable.balance.columnType
                    )
                }
            ) {
                it[BalancesTable.playerUUID] = uuid
                it[BalancesTable.currencyId] = currency.id
                it[BalancesTable.balance] = amount
            }.single()[BalancesTable.balance]
        }
    }

    suspend fun setAndGet(uuid: UUID, amount: BigDecimal, currency: VirtualCurrency): BigDecimal = withContext(VirtualsCtx) {
        dbQuery {
            storeBalance(uuid, amount, currency)
            amount
        }
    }

    suspend fun tryTake(uuid: UUID, amount: BigDecimal, currency: VirtualCurrency): BigDecimal? = withContext(VirtualsCtx) {
        dbQuery {
            BalancesTable.updateReturning(
                returning = listOf(BalancesTable.balance),
                where = {
                    (BalancesTable.playerUUID eq uuid) and
                        (BalancesTable.currencyId eq currency.id) and
                        (BalancesTable.balance greaterEq amount)
                }
            ) {
                it[BalancesTable.balance] = MinusOp(
                    BalancesTable.balance,
                    BalancesTable.balance.wrap(amount),
                    BalancesTable.balance.columnType
                )
            }.singleOrNull()?.get(BalancesTable.balance)
        }
    }

    suspend fun getLeaderboard(currency: VirtualCurrency, limit: Int = 10): List<Pair<UUID, BigDecimal>> =
        withContext(VirtualsCtx) {
            dbQuery {
                BalancesTable.select(BalancesTable.playerUUID, BalancesTable.balance)
                    .where { BalancesTable.currencyId eq currency.id }
                    .orderBy(BalancesTable.balance to SortOrder.DESC)
                    .limit(limit)
                    .map { it[BalancesTable.playerUUID] to it[BalancesTable.balance] }
            }
        }

    suspend fun getPlayerRank(uuid: UUID, currency: VirtualCurrency): Long = withContext(VirtualsCtx) {
        dbQuery {
            val playerBalance = BalancesTable.select(BalancesTable.balance)
                .where { (BalancesTable.playerUUID eq uuid) and (BalancesTable.currencyId eq currency.id) }
                .singleOrNull()?.get(BalancesTable.balance) ?: return@dbQuery -1L

            BalancesTable.select(BalancesTable.playerUUID)
                .where { (BalancesTable.currencyId eq currency.id) and (BalancesTable.balance greater playerBalance) }
                .count() + 1
        }
    }
}
