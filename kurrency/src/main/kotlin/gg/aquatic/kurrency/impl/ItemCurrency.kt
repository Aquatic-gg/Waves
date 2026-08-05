package gg.aquatic.kurrency.impl

import gg.aquatic.common.coroutine.BukkitCtx
import gg.aquatic.kurrency.Currency
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal
import java.math.RoundingMode

class ItemCurrency(
    val item: ItemStack, override val id: String, override val prefix: String = "", override val suffix: String = ""
) : Currency {
    override suspend fun give(player: Player, amount: BigDecimal) = withContext(BukkitCtx.ofEntity(player)) {
        // Physical items are discrete; we ignore decimals for the actual item count
        val toAddCount = amount.toInt()
        if (toAddCount < 1) return@withContext

        val stack = item.clone()
        var remaining = toAddCount

        while (remaining > 0) {
            val batchSize = remaining.coerceAtMost(stack.maxStackSize)
            stack.amount = batchSize
            val leftover = player.inventory.addItem(stack)

            if (leftover.isNotEmpty()) {
                leftover.values.forEach { player.world.dropItemNaturally(player.location, it) }
                break
            }
            remaining -= batchSize
        }
    }

    override suspend fun take(player: Player, amount: BigDecimal) = withContext(BukkitCtx.ofEntity(player)) {
        val toRemoveCount = amount.toInt()
        if (toRemoveCount < 1) return@withContext

        val toRemove = item.clone()
        toRemove.amount = toRemoveCount
        player.inventory.removeItem(toRemove)
    }

    override suspend fun set(player: Player, amount: BigDecimal) = withContext(BukkitCtx.ofEntity(player)) {
        val balance = getBalance(player)
        val comparison = balance.compareTo(amount)

        if (comparison < 0) {
            give(player, amount.subtract(balance))
        } else if (comparison > 0) {
            take(player, balance.subtract(amount))
        }
    }

    override suspend fun getBalance(player: Player): BigDecimal = withContext(BukkitCtx.ofEntity(player)) {
        var total = 0L
        for (content in player.inventory.contents) {
            if (content?.isSimilar(item) == true) {
                total += content.amount
            }
        }
        return@withContext BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_DOWN)
    }

    override suspend fun tryTake(player: Player, amount: BigDecimal): Boolean =
        withContext(BukkitCtx.ofEntity(player)) {
            if (getBalance(player) < amount) {
                return@withContext false
            }
            take(player, amount)
            return@withContext true
        }
}