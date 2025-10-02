package gg.aquatic.waves.profile.module.impl.economy

import gg.aquatic.waves.data.DataDriver
import gg.aquatic.waves.Waves
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.profile.AquaticPlayer
import gg.aquatic.waves.profile.ProfilesModule
import gg.aquatic.waves.registry.WavesRegistry
import java.sql.Connection

object CurrencyDriver {

    val driver: DataDriver = (Waves.INSTANCE.modules[WaveModules.PROFILES] as ProfilesModule).driver

    fun get(aquaticPlayer: AquaticPlayer): EconomyEntry {
        val places = EconomyProfileModule.getLeaderboardPlaces(aquaticPlayer)
        return driver.executeQuery("SELECT * FROM aquaticcurrency WHERE id = ?",
            {
                setInt(1, aquaticPlayer.index)
            },
            {
                val entry = EconomyEntry(aquaticPlayer, places)
                while (next()) {
                    val currencyId = getInt("currency_id")
                    val balance = getDouble("balance")

                    val currency = WavesRegistry.INDEX_TO_CURRENCY[currencyId] ?: continue
                    entry.balance[currency] = balance to balance
                }
                entry
            }
        )
    }

    fun save(connection: Connection, entry: EconomyEntry) {

        val newValues = entry.balance.mapValues { (_, pair) ->
            (pair.first to pair.first)
        }
        connection.prepareStatement("replace into aquaticcurrency values (?, ?, ?)").use { preparedStatement ->
            for ((currency, pair) in entry.balance) {
                val (balance, previous) = pair
                if (balance == previous) {
                    continue
                }
                preparedStatement.setInt(1, entry.aquaticPlayer.index)
                preparedStatement.setInt(2, currency.index)
                preparedStatement.setDouble(3, balance)
                preparedStatement.addBatch()
            }
            preparedStatement.executeBatch()
        }
        entry.balance += newValues
    }
}