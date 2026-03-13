package gg.aquatic.kurrency

import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey
import gg.aquatic.kurrency.Kurrency.bootstrapHolder
import org.bukkit.entity.Player
import java.math.BigDecimal

interface Currency {

    companion object {
        val REGISTRY_KEY = RegistryKey.simple<String, Currency>(RegistryId("aquatic", "currency"))
        val REGISTRY: Registry<String, Currency>
            get() {
                return bootstrapHolder[REGISTRY_KEY]
            }
    }

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