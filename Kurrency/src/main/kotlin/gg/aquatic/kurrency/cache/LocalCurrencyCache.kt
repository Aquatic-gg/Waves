package gg.aquatic.kurrency.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import gg.aquatic.common.coroutine.VirtualsCtx
import gg.aquatic.kurrency.CurrencyCache
import gg.aquatic.kurrency.db.CurrencyDBHandler
import gg.aquatic.kurrency.impl.RegisteredCurrency
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class LocalCurrencyCache(
    private val dbHandler: CurrencyDBHandler,
    ttlMinutes: Long = 10
) : CurrencyCache {

    private val cache: Cache<UUID, Map<RegisteredCurrency, BigDecimal>> = Caffeine.newBuilder()
        .expireAfterAccess(ttlMinutes, TimeUnit.MINUTES)
        .build()

    // Lock for the DB Loading process (Per player)
    private val loadLocks = ConcurrentHashMap<UUID, Mutex>()

    override suspend fun get(uuid: UUID, registeredCurrency: RegisteredCurrency): BigDecimal {
        val currentMap = cache.getIfPresent(uuid)
        currentMap?.get(registeredCurrency)?.let { return it }

        // Use the per-player lock only for loading the whole profile
        return loadLocks.getOrPut(uuid) { Mutex() }.withLock {
            val secondCheck = cache.getIfPresent(uuid)
            secondCheck?.get(registeredCurrency)?.let { return@withLock it }

            val freshMap = loadFromDb(uuid)
            freshMap[registeredCurrency] ?: BigDecimal.ZERO
        }
    }

    private suspend fun loadFromDb(uuid: UUID): Map<RegisteredCurrency, BigDecimal> {
        return withContext(VirtualsCtx) {
            val allDbBalances = dbHandler.getAllBalances(uuid)
            val currencies = RegisteredCurrency.REGISTRY.all()
            val map = HashMap<RegisteredCurrency, BigDecimal>()
            for ((key, balance) in allDbBalances) {
                val registered = currencies[key] ?: continue
                map[registered] = balance.setScale(2, RoundingMode.HALF_DOWN)
            }
            cache.put(uuid, map)
            map
        }
    }

    override suspend fun update(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        withContext(VirtualsCtx) {
            dbHandler.give(uuid, amount, registeredCurrency)
        }

        val oldMap = cache.getIfPresent(uuid) ?: loadFromDb(uuid)
        val newMap = oldMap.toMutableMap()
        val current = newMap[registeredCurrency] ?: BigDecimal.ZERO
        newMap[registeredCurrency] = current.add(amount).setScale(2, RoundingMode.HALF_DOWN)
        cache.put(uuid, newMap)
    }

    override suspend fun set(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        val scaledAmount = amount.setScale(2, RoundingMode.HALF_DOWN)

        withContext(VirtualsCtx) {
            dbHandler.set(uuid, scaledAmount, registeredCurrency)
        }

        val oldMap = cache.getIfPresent(uuid) ?: loadFromDb(uuid)
        val newMap = oldMap.toMutableMap()
        newMap[registeredCurrency] = scaledAmount
        cache.put(uuid, newMap)
    }

    override suspend fun getMultiple(
        uuids: Collection<UUID>,
        registeredCurrency: RegisteredCurrency
    ): Map<UUID, BigDecimal> {
        return uuids.associateWith { get(it, registeredCurrency) }
    }



    fun invalidate(uuid: UUID) {
        cache.invalidate(uuid)
    }

    fun setLocalOnly(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        val scaled = amount.setScale(2, RoundingMode.HALF_DOWN)
        val currentMap = cache.getIfPresent(uuid) ?: return

        val newMap = currentMap.toMutableMap()
        newMap[registeredCurrency] = scaled
        cache.put(uuid, newMap)
    }
}
