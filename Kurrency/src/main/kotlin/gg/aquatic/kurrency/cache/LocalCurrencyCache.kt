package gg.aquatic.kurrency.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import gg.aquatic.common.coroutine.VirtualsCtx
import gg.aquatic.kurrency.CurrencyCache
import gg.aquatic.kurrency.db.CurrencyDBHandler
import gg.aquatic.kurrency.impl.VirtualCurrency
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

    private val cache: Cache<UUID, Map<VirtualCurrency, BigDecimal>> = Caffeine.newBuilder()
        .expireAfterAccess(ttlMinutes, TimeUnit.MINUTES)
        .build()

    // Lock for the DB Loading process (Per player)
    private val loadLocks = ConcurrentHashMap<UUID, Mutex>()

    override suspend fun get(uuid: UUID, virtualCurrency: VirtualCurrency): BigDecimal {
        val currentMap = cache.getIfPresent(uuid)
        currentMap?.get(virtualCurrency)?.let { return it }

        // Use the per-player lock only for loading the whole profile
        return loadLocks.getOrPut(uuid) { Mutex() }.withLock {
            val secondCheck = cache.getIfPresent(uuid)
            secondCheck?.get(virtualCurrency)?.let { return@withLock it }

            val freshMap = loadFromDb(uuid)
            freshMap[virtualCurrency] ?: BigDecimal.ZERO
        }
    }

    private suspend fun loadFromDb(uuid: UUID): Map<VirtualCurrency, BigDecimal> {
        return withContext(VirtualsCtx) {
            val allDbBalances = dbHandler.getAllBalances(uuid)
            val currencies = VirtualCurrency.REGISTRY.all()
            val map = HashMap<VirtualCurrency, BigDecimal>()
            for ((key, balance) in allDbBalances) {
                val currency = currencies[key] ?: continue
                map[currency] = balance.setScale(2, RoundingMode.HALF_DOWN)
            }
            cache.put(uuid, map)
            map
        }
    }

    override suspend fun put(uuid: UUID, amount: BigDecimal, virtualCurrency: VirtualCurrency) {
        val scaledAmount = amount.setScale(2, RoundingMode.HALF_DOWN)
        val oldMap = cache.getIfPresent(uuid) ?: emptyMap()
        val newMap = oldMap.toMutableMap()
        newMap[virtualCurrency] = scaledAmount
        cache.put(uuid, newMap)
    }

    override suspend fun getMultiple(
        uuids: Collection<UUID>,
        virtualCurrency: VirtualCurrency
    ): Map<UUID, BigDecimal> {
        return uuids.associateWith { get(it, virtualCurrency) }
    }



    override suspend fun invalidate(uuid: UUID, virtualCurrency: VirtualCurrency?) {
        if (virtualCurrency == null) {
            cache.invalidate(uuid)
            return
        }

        val currentMap = cache.getIfPresent(uuid) ?: return
        val newMap = currentMap.toMutableMap()
        newMap.remove(virtualCurrency)
        if (newMap.isEmpty()) {
            cache.invalidate(uuid)
        } else {
            cache.put(uuid, newMap)
        }
    }

    fun setLocalOnly(uuid: UUID, amount: BigDecimal, virtualCurrency: VirtualCurrency) {
        val scaled = amount.setScale(2, RoundingMode.HALF_DOWN)
        val currentMap = cache.getIfPresent(uuid) ?: return

        val newMap = currentMap.toMutableMap()
        newMap[virtualCurrency] = scaled
        cache.put(uuid, newMap)
    }
}
