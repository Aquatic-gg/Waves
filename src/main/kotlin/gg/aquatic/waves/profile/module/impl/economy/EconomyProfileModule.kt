package gg.aquatic.waves.profile.module.impl.economy

import gg.aquatic.waves.data.MySqlDriver
import gg.aquatic.waves.economy.CustomCurrency
import gg.aquatic.waves.economy.RegisteredCurrency
import gg.aquatic.waves.profile.AquaticPlayer
import gg.aquatic.waves.profile.ProfilesModule
import gg.aquatic.waves.profile.module.ProfileModule
import gg.aquatic.waves.profile.module.ProfileModuleEntry
import java.sql.Connection
import java.util.*
import kotlin.collections.HashMap

object EconomyProfileModule : ProfileModule {
    override val id: String = "aquaticeconomy"

    val leaderboards = HashMap<RegisteredCurrency, EconomyLeaderboard>()

    override fun loadEntry(player: AquaticPlayer): ProfileModuleEntry {
        return CurrencyDriver.get(player)
    }

    fun initializeEconomy(currency: CustomCurrency): RegisteredCurrency {
        return CurrencyDriver.driver.executeQuery("SELECT * FROM aquaticcurrency_type WHERE currency_id = ?",
            {
                setString(1, currency.id)
            },
            {
                if (next()) {
                    val id = getInt("id")
                    return@executeQuery Optional.of(RegisteredCurrency(currency, id))
                } else
                    return@executeQuery Optional.empty<RegisteredCurrency>()
            }
        ).orElseGet {
            CurrencyDriver.driver.useConnection {
                prepareStatement("INSERT INTO aquaticcurrency_type (currency_id) VALUES (?)").use { preparedStatement ->
                    preparedStatement.setString(1, currency.id)
                    preparedStatement.execute()
                    val rs = preparedStatement.generatedKeys
                    rs.next()
                    val id = rs.getInt(1)
                    RegisteredCurrency(currency, id)
                }
            }
        }
    }

    override fun initialize(connection: Connection) {
        if (ProfilesModule.driver is MySqlDriver) {
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS " +
                        "aquaticcurrency_type (" +
                        "id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                        "currency_id NVARCHAR(64) NOT NULL UNIQUE" +
                        ")"
            ).use { preparedStatement ->
                preparedStatement.execute()
            }
        } else {
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS " +
                        "aquaticcurrency_type (" +
                        "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                        "currency_id NVARCHAR(64) NOT NULL UNIQUE" +
                        ")"
            ).use { preparedStatement ->
                preparedStatement.execute()
            }
        }

        connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS " +
                    "aquaticcurrency (" +
                    "id INTEGER NOT NULL," +
                    "currency_id INTEGER NOT NULL," +
                    "balance DECIMAL NOT NULL," +
                    "PRIMARY KEY (id, currency_id)," +
                    "FOREIGN KEY (id) REFERENCES aquaticprofiles(id)," +
                    "FOREIGN KEY (currency_id) REFERENCES aquaticcurrency_type(id)" +
                    ")"
        ).use { preparedStatement ->
            preparedStatement.execute()
        }
    }

}

fun AquaticPlayer.aquaticEconomy(): EconomyEntry {
    if (!this.entries.containsKey("aquaticeconomy")) {
        val places = HashMap<RegisteredCurrency, Int>()
        for ((currency, leaderboard) in EconomyProfileModule.leaderboards) {
            places += currency to leaderboard.totalPlaces
        }
        val entry = EconomyEntry(this, places)
        this.entries += "aquaticeconomy" to entry
        return entry
    }
    return this.entries["aquaticeconomy"] as EconomyEntry
}