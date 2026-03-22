package gg.aquatic.kurrency

import gg.aquatic.kevent.SuspendingEventBus
import gg.aquatic.kevent.suspendingEventBusBuilder
import gg.aquatic.kurrency.db.CurrencyDBHandler
import gg.aquatic.kurrency.event.CurrencyTransactionEvent
import gg.aquatic.kurrency.impl.RegisteredCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.EmptyCoroutineContext

class CurrencyHandler(
    val cache: CurrencyCache,
    val dbHandler: CurrencyDBHandler
) {
    private val locks = ConcurrentHashMap<Pair<UUID, String>, Mutex>()

    val eventBus: SuspendingEventBus = suspendingEventBusBuilder {
        scope = CoroutineScope(EmptyCoroutineContext)
    }

    private fun getLock(uuid: UUID, currency: RegisteredCurrency) =
        locks.computeIfAbsent(uuid to currency.id) { Mutex() }

    private suspend fun <T> withCurrencyLock(
        uuid: UUID,
        currency: RegisteredCurrency,
        block: suspend () -> T
    ): T = withTimeout(5000) {
        getLock(uuid, currency).withLock { block() }
    }

    private suspend fun postTransactionIfChanged(
        uuid: UUID,
        currency: RegisteredCurrency,
        previousBalance: BigDecimal,
        newBalance: BigDecimal
    ) {
        if (previousBalance.compareTo(newBalance) == 0) {
            return
        }

        eventBus.postSuspend(
            CurrencyTransactionEvent(uuid, currency, previousBalance, newBalance, newBalance.subtract(previousBalance))
        )
    }

    suspend fun getBalance(uuid: UUID, currency: RegisteredCurrency): BigDecimal =
        withCurrencyLock(uuid, currency) {
            val balance = dbHandler.getBalance(uuid, currency).setScale(2, RoundingMode.HALF_DOWN)
            cache.put(uuid, balance, currency)
            balance
        }

    suspend fun give(uuid: UUID, currency: RegisteredCurrency, amount: BigDecimal) {
        require(amount >= BigDecimal.ZERO) { "Amount must be positive. Use tryTake to deduct." }
        withCurrencyLock(uuid, currency) {
            val current = dbHandler.getBalance(uuid, currency).setScale(2, RoundingMode.HALF_DOWN)
            val newBalance = dbHandler.changeBy(uuid, amount.setScale(2, RoundingMode.HALF_DOWN), currency)
                .setScale(2, RoundingMode.HALF_DOWN)
            cache.put(uuid, newBalance, currency)
            postTransactionIfChanged(uuid, currency, current, newBalance)
        }
    }

    suspend fun set(uuid: UUID, currency: RegisteredCurrency, amount: BigDecimal) =
        withCurrencyLock(uuid, currency) {
            val scaledAmount = amount.setScale(2, RoundingMode.HALF_DOWN)
            val current = dbHandler.getBalance(uuid, currency).setScale(2, RoundingMode.HALF_DOWN)
            val newBalance = dbHandler.setAndGet(uuid, scaledAmount, currency).setScale(2, RoundingMode.HALF_DOWN)
            cache.put(uuid, newBalance, currency)
            postTransactionIfChanged(uuid, currency, current, newBalance)
        }

    suspend fun take(uuid: UUID, currency: RegisteredCurrency, amount: BigDecimal): Boolean {
        require(amount >= BigDecimal.ZERO) { "Amount to take must be positive." }
        return withCurrencyLock(uuid, currency) {
            val current = dbHandler.getBalance(uuid, currency).setScale(2, RoundingMode.HALF_DOWN)
            val newBalance = dbHandler.tryTake(uuid, amount.setScale(2, RoundingMode.HALF_DOWN), currency)
                ?.setScale(2, RoundingMode.HALF_DOWN)
                ?: return@withCurrencyLock false
            cache.put(uuid, newBalance, currency)
            postTransactionIfChanged(uuid, currency, current, newBalance)
            true
        }
    }

    suspend fun tryTake(uuid: UUID, currency: RegisteredCurrency, amount: BigDecimal): Boolean {
        return take(uuid, currency, amount)
    }

    fun cleanup(player: Player) {
        locks.keys.removeIf { it.first == player.uniqueId }
    }
}
