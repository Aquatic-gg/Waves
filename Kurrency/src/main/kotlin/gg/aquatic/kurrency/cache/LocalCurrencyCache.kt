package gg.aquatic.kurrency.cache

import gg.aquatic.common.coroutine.BukkitCtx
import gg.aquatic.common.coroutine.VirtualsCtx
import gg.aquatic.common.event
import gg.aquatic.kurrency.CurrencyCache
import gg.aquatic.kurrency.db.CurrencyDBHandler
import gg.aquatic.kurrency.impl.RegisteredCurrency
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class LocalCurrencyCache(
    private val dbHandler: CurrencyDBHandler,
    private val saveIntervalSeconds: Int = 900 // 15 Minutes
) : CurrencyCache {

    private val balances = HashMap<UUID, HashMap<RegisteredCurrency, BigDecimal>>()

    init {
        event<PlayerJoinEvent> {
            val uuid = it.player.uniqueId
            BukkitCtx.GLOBAL {
                balances.getOrPut(uuid) { HashMap() }
            }
        }

        event<PlayerQuitEvent> {
            event<PlayerJoinEvent> {
                val uuid = it.player.uniqueId
                BukkitCtx.GLOBAL.launch {
                    val playerMap = balances.getOrPut(uuid) { HashMap() }

                    val allDbBalances = dbHandler.getAllBalances(uuid)

                    val currencies = RegisteredCurrency.REGISTRY.getAll()
                    for ((id, balance) in allDbBalances) {

                        val registered = currencies[id] ?: continue
                        playerMap[registered] = balance
                    }
                }
            }
        }
    }

    override suspend fun isActive(uuid: UUID): Boolean = withContext(BukkitCtx.GLOBAL) {
        balances.containsKey(uuid)
    }

    override suspend fun get(uuid: UUID, registeredCurrency: RegisteredCurrency): BigDecimal =
        withContext(BukkitCtx.GLOBAL) {
            val playerMap = balances[uuid] ?: return@withContext dbHandler.getBalance(uuid, registeredCurrency)

            playerMap[registeredCurrency] ?: dbHandler.getBalance(uuid, registeredCurrency).also {
                playerMap[registeredCurrency] = it
            }
        }

    override suspend fun getMultiple(
        uuids: Collection<UUID>,
        registeredCurrency: RegisteredCurrency
    ): Map<UUID, BigDecimal> =
        withContext(BukkitCtx.GLOBAL) {
            uuids.associateWith { get(it, registeredCurrency) }
        }

    override suspend fun update(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        withContext(BukkitCtx.GLOBAL) {
            val playerMap = balances[uuid]
            if (playerMap != null) {
                val current = playerMap.getOrDefault(registeredCurrency, BigDecimal.ZERO)
                playerMap[registeredCurrency] = current.add(amount).setScale(2, RoundingMode.HALF_DOWN)
            } else {
                dbHandler.give(uuid, amount, registeredCurrency)
            }
        }
    }

    override suspend fun set(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        withContext(BukkitCtx.GLOBAL) {
            val playerMap = balances[uuid]
            if (playerMap != null) {
                playerMap[registeredCurrency] = amount.setScale(2, RoundingMode.HALF_DOWN)
            } else {
                dbHandler.set(uuid, amount, registeredCurrency)
            }
        }
    }

    private suspend fun save(uuid: UUID) = withContext(BukkitCtx.GLOBAL) {
        val playerMap = balances[uuid] ?: return@withContext

        withContext(VirtualsCtx) {
            for ((currency, balance) in playerMap) {
                dbHandler.set(uuid, balance, currency)
            }
        }
    }

    /**
     * Updates the local memory cache ONLY.
     * Use this when handling external synchronization to avoid redundant DB writes.
     */
    fun setLocalOnly(uuid: UUID, amount: BigDecimal, registeredCurrency: RegisteredCurrency) {
        val run = run@{
            val playerMap = balances[uuid] ?: return@run
            playerMap[registeredCurrency] = amount.setScale(2, RoundingMode.HALF_DOWN)
        }
        if (!Bukkit.isPrimaryThread()) {
            BukkitCtx.GLOBAL.launch { run() }
        } else {
            run()
        }
    }
}
