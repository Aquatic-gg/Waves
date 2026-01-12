package gg.aquatic.kurrency

import gg.aquatic.kevent.EventBus
import gg.aquatic.kevent.eventBusBuilder
import gg.aquatic.kurrency.event.CurrencyTransactionEvent
import gg.aquatic.kurrency.impl.RegisteredCurrency
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CurrencyHandler(
    val cache: CurrencyCache
) {
    private val locks = ConcurrentHashMap<Pair<UUID, String>, Mutex>()

    val eventBus: EventBus = eventBusBuilder {
    }

    private fun getLock(uuid: UUID, currency: RegisteredCurrency) =
        locks.computeIfAbsent(uuid to currency.id) { Mutex() }

    suspend fun <T> withTransaction(
        uuid: UUID,
        currency: RegisteredCurrency,
        block: suspend (currentBalance: BigDecimal) -> T
    ): T = withTimeout(5000) {
        getLock(uuid, currency).withLock {
            val current = (cache.get(uuid, currency) ?: BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_DOWN)

            val result = block(current)
            val newBalance = (cache.get(uuid, currency) ?: BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_DOWN)
            if (current.compareTo(newBalance) != 0) {
                val change = newBalance.subtract(current)

                eventBus.postSuspend(
                    CurrencyTransactionEvent(uuid, currency, current, newBalance, change)
                )
            }

            result
        }
    }

    suspend fun getBalance(uuid: UUID, currency: RegisteredCurrency): BigDecimal =
        withTransaction(uuid, currency) { it }

    suspend fun give(uuid: UUID, currency: RegisteredCurrency, amount: BigDecimal) {
        require(amount >= BigDecimal.ZERO) { "Amount must be positive. Use tryTake to deduct." }
        withTransaction(uuid, currency) {
            cache.update(uuid, amount, currency)
        }
    }

    suspend fun set(uuid: UUID, currency: RegisteredCurrency, amount: BigDecimal) =
        withTransaction(uuid, currency) {
            cache.set(uuid, amount, currency)
        }

    suspend fun tryTake(uuid: UUID, currency: RegisteredCurrency, amount: BigDecimal): Boolean {
        require(amount >= BigDecimal.ZERO) { "Amount to take must be positive." }
        return withTransaction(uuid, currency) { current ->
            if (current < amount) return@withTransaction false

            val newBalance = current.subtract(amount)
            if (newBalance < BigDecimal.ZERO) return@withTransaction false

            cache.update(uuid, amount.negate(), currency)
            true
        }
    }

    fun cleanup(player: Player) {
        locks.keys.removeIf { it.first == player.uniqueId }
    }
}