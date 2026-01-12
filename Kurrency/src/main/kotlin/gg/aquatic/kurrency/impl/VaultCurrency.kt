package gg.aquatic.kurrency.impl

import gg.aquatic.kurrency.Currency
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.math.RoundingMode

class VaultCurrency(
    override val id: String = "vault",
    override val prefix: String = "",
    override val suffix: String = ""
) : Currency {

    private val econ: Economy? by lazy {
        Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider
    }

    override suspend fun give(player: Player, amount: BigDecimal) {
        econ?.depositPlayer(player, amount.toDouble())
    }

    override suspend fun take(player: Player, amount: BigDecimal) {
        econ?.withdrawPlayer(player, amount.toDouble())
    }

    override suspend fun set(player: Player, amount: BigDecimal) {
        val current = getBalance(player)
        if (current < amount) give(player, amount.subtract(current))
        else take(player, current.subtract(amount))
    }

    override suspend fun getBalance(player: Player): BigDecimal {
        return BigDecimal.valueOf(econ?.getBalance(player) ?: 0.0).setScale(2, RoundingMode.HALF_DOWN)
    }

    override suspend fun tryTake(player: Player, amount: BigDecimal): Boolean {
        if (getBalance(player) < amount) return false
        return econ?.withdrawPlayer(player, amount.toDouble())?.transactionSuccess() ?: false
    }
}
