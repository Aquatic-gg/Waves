package gg.aquatic.kurrency

import gg.aquatic.common.HikariDBFactory
import gg.aquatic.kregistry.bootstrap.BootstrapHolder
import gg.aquatic.kurrency.db.BalancesTable
import gg.aquatic.kurrency.db.CurrencyDBHandler
import gg.aquatic.kurrency.impl.VirtualCurrency
import org.jetbrains.exposed.v1.jdbc.Database

object Kurrency {

    lateinit var bootstrapHolder: BootstrapHolder
    lateinit var database: Database
    lateinit var dbHandler: CurrencyDBHandler
    lateinit var currencyHandler: CurrencyHandler

    fun getCurrency(id: String): Currency? {
        return Currency.REGISTRY[id]
    }

    fun getVirtualCurrency(id: String): VirtualCurrency? {
        return VirtualCurrency.REGISTRY[id]
    }
}

fun BootstrapHolder.initializeKurrency(
    dbUrl: String, dbDriver: String, dbUser: String, dbPass: String,
    cache: (Database, CurrencyDBHandler) -> CurrencyCache,
    currencies: List<Currency> = emptyList(),
    dbCurrencies: List<VirtualCurrency> = emptyList(),
) {
    val database = HikariDBFactory.init(dbUrl, dbDriver, dbUser, dbPass, BalancesTable)
    val dbHandler = CurrencyDBHandler(database)

    Kurrency.bootstrapHolder = this
    Kurrency.database = database
    Kurrency.dbHandler = dbHandler
    Kurrency.currencyHandler = CurrencyHandler(cache(database, dbHandler), dbHandler)
    KurrencyRegistryHolder.registryBootstrap(this) {
        registry(Currency.REGISTRY_KEY) {
            currencies.forEach { add(it.id, it) }
            dbCurrencies.forEach { add(it.id, it) }
        }
        registry(VirtualCurrency.REGISTRY_KEY) {
            dbCurrencies.forEach { currency -> add(currency.id, currency) }
        }
    }
}

