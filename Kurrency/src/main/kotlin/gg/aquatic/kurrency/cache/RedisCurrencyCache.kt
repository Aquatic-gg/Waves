package gg.aquatic.kurrency.cache

import gg.aquatic.common.coroutine.BukkitCtx
import gg.aquatic.common.coroutine.VirtualsCtx
import gg.aquatic.kurrency.CurrencyCache
import gg.aquatic.kurrency.impl.RegisteredCurrency
import gg.aquatic.kurrency.db.CurrencyDBHandler
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import redis.clients.jedis.UnifiedJedis
import redis.clients.jedis.params.SetParams
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.iterator

class RedisCurrencyCache(
    private val jedis: UnifiedJedis,
    private val dbHandler: CurrencyDBHandler,
    private val ttlSeconds: Long = 1800 // Default 30 minutes
) : CurrencyCache {

    private val keyPrefix = "currency:"

    private fun playerKey(uuid: UUID, currency: RegisteredCurrency) =
        "$keyPrefix${currency.id}:$uuid"

    override suspend fun isActive(uuid: UUID): Boolean {
        return withContext(BukkitCtx.GLOBAL) { Bukkit.getPlayer(uuid)?.isConnected == true } || withContext(VirtualsCtx) {
            jedis.exists("$keyPrefix*:$uuid")
        }
    }

    override suspend fun get(uuid: UUID, registeredCurrency: RegisteredCurrency): BigDecimal? =
        withContext(VirtualsCtx) {
            val key = playerKey(uuid, registeredCurrency)
            val data = jedis.get(key)

            if (data != null) {
                jedis.expire(key, ttlSeconds)
                return@withContext data.toBigDecimalOrNull()?.setScale(2, RoundingMode.HALF_DOWN)
            }

            val dbBalance = dbHandler.getBalance(uuid, registeredCurrency)
            jedis.set(key, dbBalance.toPlainString(), SetParams().ex(ttlSeconds))
            dbBalance
        }

    override suspend fun getMultiple(
        uuids: Collection<UUID>,
        registeredCurrency: RegisteredCurrency
    ): Map<UUID, BigDecimal> = withContext(VirtualsCtx) {
        val results = mutableMapOf<UUID, BigDecimal>()
        val missingUuids = mutableListOf<UUID>()

        for (uuid in uuids) {
            val key = playerKey(uuid, registeredCurrency)
            val data = jedis.get(key)
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

            val current = get(uuid, registeredCurrency) ?: BigDecimal.ZERO
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