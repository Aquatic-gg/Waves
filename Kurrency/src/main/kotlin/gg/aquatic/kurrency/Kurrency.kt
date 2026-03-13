package gg.aquatic.kurrency

import gg.aquatic.common.HikariDBFactory
import gg.aquatic.kregistry.bootstrap.BootstrapHolder
import gg.aquatic.kurrency.db.BalancesTable
import gg.aquatic.kurrency.impl.RegisteredCurrency
import gg.aquatic.kurrency.impl.VirtualCurrency
import org.jetbrains.exposed.v1.jdbc.Database

object Kurrency {

    lateinit var bootstrapHolder: BootstrapHolder
    lateinit var database: Database
    lateinit var currencyHandler: CurrencyHandler

    fun getCurrency(id: String): Currency? {
        return Currency.REGISTRY[id]
    }
}

fun BootstrapHolder.initializeKurrency(
    dbUrl: String, dbDriver: String, dbUser: String, dbPass: String,
    cache: CurrencyCache,
    currencies: List<Currency> = emptyList(),
    dbCurrencies: List<VirtualCurrency> = emptyList(),
) {
    val database = HikariDBFactory.init(dbUrl, dbDriver, dbUser, dbPass, BalancesTable)

    Kurrency.bootstrapHolder = this
    Kurrency.database = database
    Kurrency.currencyHandler = CurrencyHandler(cache)

    KurrencyRegistryHolder.registryBootstrap(this) {
        registry(Currency.REGISTRY_KEY) {
            currencies.forEach { add(it.id, it) }
        }
        registry(RegisteredCurrency.REGISTRY_KEY) {
            dbCurrencies.forEach { add(it.id, RegisteredCurrency(it)) }
        }
    }
}

