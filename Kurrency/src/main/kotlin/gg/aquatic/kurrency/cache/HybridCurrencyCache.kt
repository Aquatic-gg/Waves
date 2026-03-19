package gg.aquatic.kurrency.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import gg.aquatic.kurrency.CurrencyCache
import gg.aquatic.kurrency.impl.RegisteredCurrency
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Hybrid implementation of the `CurrencyCache` interface that combines a local in-memory cache
 * and a parent cache (e.g. a database or a distributed cache) to achieve both high performance
 * and persistence. Frequently accessed data is cached locally for faster retrieval, while all
 * operations maintain consistency with the parent cache to ensure data durability and accuracy.
 *
 * @constructor Initializes the `HybridCurrencyCache` with the specified parent cache and a configurable
 * local time-to-live (TTL) for cached entries.
 * @param parent The delegate `CurrencyCache` that acts as the primary data source and persistent storage.
 * @param localTtlMinutes The duration (in minutes) before local cache entries expire after the last access.
 */
class HybridCurrencyCache(
    private val parent: CurrencyCache,
    localTtlMinutes: Long = 10
) : CurrencyCache {

    private val localCache: Cache<UUID, MutableMap<RegisteredCurrency, BigDecimal>> = Caffeine.newBuilder()
        .expireAfterAccess(localTtlMinutes, TimeUnit.MINUTES)
        .build()

    /**
     * Synchronous access for PlaceholderAPI.
     * Only checks local memory.
     */
    fun getIfCached(uuid: UUID, registeredCurrency: RegisteredCurrency): BigDecimal? {
        return localCache.getIfPresent(uuid)?.get(registeredCurrency)
    }

    override suspend fun get(uuid: UUID, registeredCurrency: RegisteredCurrency): BigDecimal {
        // Check local first
        val map = localCache.getIfPresent(uuid)
        val cached = map?.get(registeredCurrency)
        if (cached != null) return cached

        // Fetch from parent (Redis or DB) - now non-nullable
        val balance = parent.get(uuid, registeredCurrency)

        // Update local
        val activeMap = localCache.get(uuid) { HashMap() }
        activeMap[registeredCurrency] = balance

        return balance
    }

    override suspend fun put(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        val scaled = amount.setScale(2, RoundingMode.HALF_DOWN)
        parent.put(uuid, scaled, registeredCurrency)
        localCache.get(uuid) { HashMap() }[registeredCurrency] = scaled
    }

    override suspend fun getMultiple(uuids: Collection<UUID>, registeredCurrency: RegisteredCurrency): Map<UUID, BigDecimal> {
        return parent.getMultiple(uuids, registeredCurrency).onEach { (uuid, balance) ->
            localCache.get(uuid) { HashMap() }[registeredCurrency] = balance
        }
    }

    override suspend fun invalidate(uuid: UUID, registeredCurrency: RegisteredCurrency?) {
        if (registeredCurrency == null) {
            localCache.invalidate(uuid)
            parent.invalidate(uuid, null)
            return
        }

        val current = localCache.getIfPresent(uuid)
        if (current != null) {
            val newMap = current.toMutableMap()
            newMap.remove(registeredCurrency)
            if (newMap.isEmpty()) {
                localCache.invalidate(uuid)
            } else {
                localCache.put(uuid, newMap)
            }
        }

        parent.invalidate(uuid, registeredCurrency)
    }
}
