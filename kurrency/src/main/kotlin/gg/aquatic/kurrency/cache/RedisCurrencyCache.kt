package gg.aquatic.kurrency.cache

import gg.aquatic.common.coroutine.VirtualsCtx
import gg.aquatic.kurrency.CurrencyCache
import gg.aquatic.kurrency.db.CurrencyDBHandler
import gg.aquatic.kurrency.impl.VirtualCurrency
import kotlinx.coroutines.withContext
import redis.clients.jedis.UnifiedJedis
import redis.clients.jedis.params.SetParams
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class RedisCurrencyCache(
    private val jedis: UnifiedJedis,
    private val dbHandler: CurrencyDBHandler,
    private val ttlSeconds: Long = 1800 // Default 30 minutes
) : CurrencyCache {

    private val keyPrefix = "currency:"

    private fun playerKey(uuid: UUID, currency: VirtualCurrency) =
        "$keyPrefix${currency.id}:$uuid"

    override suspend fun get(uuid: UUID, virtualCurrency: VirtualCurrency): BigDecimal =
        withContext(VirtualsCtx) {
            val key = playerKey(uuid, virtualCurrency)
            val data = jedis.get(key)

            if (data != null) {
                jedis.expire(key, ttlSeconds)
                return@withContext data.toBigDecimal().setScale(2, RoundingMode.HALF_DOWN)
            }

            val dbBalance = dbHandler.getBalance(uuid, virtualCurrency)
            jedis.set(key, dbBalance.toPlainString(), SetParams().ex(ttlSeconds))
            dbBalance
        }

    override suspend fun getMultiple(
        uuids: Collection<UUID>,
        virtualCurrency: VirtualCurrency
    ): Map<UUID, BigDecimal> = withContext(VirtualsCtx) {
        val uuidList = uuids.toList()
        val keys = uuidList.map { playerKey(it, virtualCurrency) }.toTypedArray()

        // Use MGET for a single network round-trip
        val values = jedis.mget(*keys)
        val results = mutableMapOf<UUID, BigDecimal>()
        val missingUuids = mutableListOf<UUID>()

        for (i in uuidList.indices) {
            val data = values[i]
            val uuid = uuidList[i]
            if (data != null) {
                results[uuid] = data.toBigDecimal().setScale(2, RoundingMode.HALF_DOWN)
            } else {
                missingUuids.add(uuid)
            }
        }

        if (missingUuids.isNotEmpty()) {
            val dbBalances = dbHandler.getBalances(missingUuids, virtualCurrency)
            for ((uuid, balance) in dbBalances) {
                jedis.set(playerKey(uuid, virtualCurrency), balance.toPlainString(), SetParams().ex(ttlSeconds))
                results[uuid] = balance
            }
        }
        results
    }

    override suspend fun put(uuid: UUID, amount: BigDecimal, virtualCurrency: VirtualCurrency) {
        withContext(VirtualsCtx) {
            val key = playerKey(uuid, virtualCurrency)
            val scaled = amount.setScale(2, RoundingMode.HALF_DOWN)
            jedis.set(key, scaled.toPlainString(), SetParams().ex(ttlSeconds))
        }
    }

    override suspend fun invalidate(uuid: UUID, virtualCurrency: VirtualCurrency?) {
        withContext(VirtualsCtx) {
            if (virtualCurrency == null) {
                return@withContext
            }
            jedis.del(playerKey(uuid, virtualCurrency))
        }
    }
}
