package gg.aquatic.kurrency

import org.bukkit.entity.Player
import java.math.BigDecimal

interface Currency {

    val id: String
    val prefix: String
    val suffix: String

    fun formatBalance(amount: BigDecimal): String {
        return "${prefix}${amount.formatBalanceWithSuffix()}${suffix}"
    }

    suspend fun getFormattedBalance(player: Player): String {
        return getBalance(player).formatBalanceWithSuffix()
    }

    suspend fun give(player: Player, amount: BigDecimal)
    suspend fun take(player: Player, amount: BigDecimal)
    suspend fun set(player: Player, amount: BigDecimal)
    suspend fun getBalance(player: Player): BigDecimal
    suspend fun tryTake(player: Player, amount: BigDecimal): Boolean

}