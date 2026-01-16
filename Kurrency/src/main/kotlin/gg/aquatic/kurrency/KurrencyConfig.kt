package gg.aquatic.kurrency

import gg.aquatic.common.HikariDBFactory
import gg.aquatic.kregistry.*
import gg.aquatic.kurrency.db.BalancesTable
import org.jetbrains.exposed.sql.Database

object KurrencyConfig {

    lateinit var database: Database
    lateinit var currencyHandler: CurrencyHandler

    val REGISTRY_KEY = RegistryKey<String, Currency>(RegistryId("aquatic", "currency"))
    val REGISTRY: FrozenRegistry<String, Currency>
        get() {
            return Registry[REGISTRY_KEY]
        }

    fun injectCurrency(currency: Currency) {
        Registry.update { replaceRegistry(REGISTRY_KEY) { register(currency.id, currency) } }
    }

    fun injectCurrencies(currencies: List<Currency>) {
        Registry.update { replaceRegistry(REGISTRY_KEY) { currencies.forEach { register(it.id, it) } } }
    }

    fun getCurrency(id: String): Currency? {
        return Registry[REGISTRY_KEY][id]
    }
}

fun initializeKurrency(
    dbUrl: String, dbDriver: String, dbUser: String, dbPass: String,
    cache: CurrencyCache,
    currencies: List<Currency> = emptyList()
) {
    val database = HikariDBFactory.init(dbUrl, dbDriver, dbUser, dbPass, BalancesTable)

    KurrencyConfig.database = database
    KurrencyConfig.currencyHandler = CurrencyHandler(cache)

    val registry = MutableRegistry<String, Currency>()
    for (currency in currencies) {
        registry.register(currency.id, currency)
    }

    Registry.update { registerRegistry(KurrencyConfig.REGISTRY_KEY, registry.freeze()) }
}

