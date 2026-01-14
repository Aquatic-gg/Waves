package gg.aquatic.kurrency.cache

import gg.aquatic.common.coroutine.VirtualsCtx
import gg.aquatic.kurrency.CurrencyCache
import gg.aquatic.kurrency.db.CurrencyDBHandler
import gg.aquatic.kurrency.impl.RegisteredCurrency
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

    private fun playerKey(uuid: UUID, currency: RegisteredCurrency) =
        "$keyPrefix${currency.id}:$uuid"

    override suspend fun get(uuid: UUID, registeredCurrency: RegisteredCurrency): BigDecimal =
        withContext(VirtualsCtx) {
            val key = playerKey(uuid, registeredCurrency)
            val data = jedis.get(key)

            if (data != null) {
                jedis.expire(key, ttlSeconds)
                return@withContext data.toBigDecimal().setScale(2, RoundingMode.HALF_DOWN)
            }

            val dbBalance = dbHandler.getBalance(uuid, registeredCurrency)
            jedis.set(key, dbBalance.toPlainString(), SetParams().ex(ttlSeconds))
            dbBalance
        }

    override suspend fun getMultiple(
        uuids: Collection<UUID>,
        registeredCurrency: RegisteredCurrency
    ): Map<UUID, BigDecimal> = withContext(VirtualsCtx) {
        val uuidList = uuids.toList()
        val keys = uuidList.map { playerKey(it, registeredCurrency) }.toTypedArray()

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
            val dbBalances = dbHandler.getBalances(missingUuids, registeredCurrency)
            for ((uuid, balance) in dbBalances) {
                jedis.set(playerKey(uuid, registeredCurrency), balance.toPlainString(), SetParams().ex(ttlSeconds))
                results[uuid] = balance
            }
        }
        results
    }

    override suspend fun update(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        withContext(VirtualsCtx) {
            val key = playerKey(uuid, registeredCurrency)
            dbHandler.give(uuid, amount, registeredCurrency)

            val current = get(uuid, registeredCurrency)
            val newBalance = current.add(amount)
            jedis.set(key, newBalance.toPlainString(), SetParams().ex(ttlSeconds))
        }
    }

    override suspend fun set(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        withContext(VirtualsCtx) {
            val key = playerKey(uuid, registeredCurrency)
            dbHandler.set(uuid, amount, registeredCurrency)
            jedis.set(key, amount.toPlainString(), SetParams().ex(ttlSeconds))
        }
    }
}