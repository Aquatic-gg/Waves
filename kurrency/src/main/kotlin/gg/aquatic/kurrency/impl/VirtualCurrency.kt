package gg.aquatic.kurrency.impl

import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey
import gg.aquatic.kurrency.Currency
import gg.aquatic.kurrency.Kurrency
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.UUID

class VirtualCurrency(
    override val id: String,
    override val prefix: String = "",
    override val suffix: String = ""
) : Currency {

    companion object {
        val REGISTRY_KEY = RegistryKey.simple<String, VirtualCurrency>(RegistryId("aquatic", "virtual_currency"))
        val REGISTRY: Registry<String, VirtualCurrency>
            get() = Kurrency.bootstrapHolder[REGISTRY_KEY]

        fun of(
            id: String,
            prefix: String = "",
            suffix: String = ""
        ): VirtualCurrency = VirtualCurrency(id, prefix, suffix)
    }

    override suspend fun getBalance(player: Player): BigDecimal = getBalance(player.uniqueId)

    override suspend fun give(player: Player, amount: BigDecimal) {
        give(player.uniqueId, amount)
    }

    override suspend fun set(player: Player, amount: BigDecimal) {
        set(player.uniqueId, amount)
    }

    override suspend fun take(player: Player, amount: BigDecimal) {
        take(player.uniqueId, amount)
    }

    override suspend fun tryTake(player: Player, amount: BigDecimal): Boolean = tryTake(player.uniqueId, amount)

    suspend fun give(uuid: UUID, amount: BigDecimal) {
        Kurrency.currencyHandler.give(uuid, this, amount)
    }

    suspend fun take(uuid: UUID, amount: BigDecimal) {
        Kurrency.currencyHandler.take(uuid, this, amount)
    }

    suspend fun set(uuid: UUID, amount: BigDecimal) {
        Kurrency.currencyHandler.set(uuid, this, amount)
    }

    suspend fun getBalance(uuid: UUID): BigDecimal {
        return Kurrency.currencyHandler.getBalance(uuid, this)
    }

    suspend fun tryTake(uuid: UUID, amount: BigDecimal): Boolean {
        return Kurrency.currencyHandler.tryTake(uuid, this, amount)
    }
}
