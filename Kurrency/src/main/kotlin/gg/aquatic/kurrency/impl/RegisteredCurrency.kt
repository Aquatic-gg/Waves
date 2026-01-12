package gg.aquatic.kurrency.impl

import gg.aquatic.kregistry.FrozenRegistry
import gg.aquatic.kregistry.Registry
import gg.aquatic.kregistry.RegistryId
import gg.aquatic.kregistry.RegistryKey
import gg.aquatic.kurrency.Currency
import org.bukkit.entity.Player
import java.math.BigDecimal

class RegisteredCurrency internal constructor(
    val currency: VirtualCurrency
) : Currency {
    override val id: String = currency.id
    override val prefix: String
        get() = currency.prefix
    override val suffix: String
        get() = currency.suffix

    companion object {
        val REGISTRY_KEY = RegistryKey<String, RegisteredCurrency>(RegistryId("aquatic", "registered_currency"))
        val REGISTRY: FrozenRegistry<String, RegisteredCurrency>
            get() {
                return Registry[REGISTRY_KEY]
            }
    }

    override suspend fun getBalance(player: Player): BigDecimal {
        return currency.getBalance(player.uniqueId, this)
    }

    override suspend fun give(player: Player, amount: BigDecimal) {
        currency.give(player.uniqueId, amount, this)
    }

    override suspend fun set(player: Player, amount: BigDecimal) {
        currency.set(player.uniqueId, amount, this)
    }

    override suspend fun take(player: Player, amount: BigDecimal) {
        currency.take(player.uniqueId, amount, this)
    }

    override suspend fun tryTake(player: Player, amount: BigDecimal): Boolean {
        return currency.tryTake(player.uniqueId, amount, this)
    }
}