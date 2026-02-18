package gg.aquatic.kurrency

import gg.aquatic.common.HikariDBFactory
import gg.aquatic.kregistry.bootstrap.BootstrapHolder
import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey
import gg.aquatic.kurrency.db.BalancesTable
import gg.aquatic.kurrency.impl.RegisteredCurrency
import gg.aquatic.kurrency.impl.VirtualCurrency
import org.jetbrains.exposed.v1.jdbc.Database

object KurrencyConfig {

    lateinit var bootstrapHolder: BootstrapHolder
    lateinit var database: Database
    lateinit var currencyHandler: CurrencyHandler

    val REGISTRY_KEY = RegistryKey.simple<String, Currency>(RegistryId("aquatic", "currency"))
    val REGISTRY: Registry<String, Currency>
        get() {
            return bootstrapHolder[REGISTRY_KEY]
        }

    fun getCurrency(id: String): Currency? {
        return REGISTRY[id]
    }
}

fun initializeKurrency(
    bootstrapHolder: BootstrapHolder,
    dbUrl: String, dbDriver: String, dbUser: String, dbPass: String,
    cache: CurrencyCache,
    currencies: List<Currency> = emptyList(),
    dbCurrencies: List<VirtualCurrency> = emptyList(),
) {
    val database = HikariDBFactory.init(dbUrl, dbDriver, dbUser, dbPass, BalancesTable)

    KurrencyConfig.bootstrapHolder = bootstrapHolder
    KurrencyConfig.database = database
    KurrencyConfig.currencyHandler = CurrencyHandler(cache)

    KurrencyRegistryHolder.registryBootstrap(bootstrapHolder) {
        registry(KurrencyConfig.REGISTRY_KEY) {
            currencies.forEach { add(it.id, it) }
        }
        registry(RegisteredCurrency.REGISTRY_KEY) {
            dbCurrencies.forEach { add(it.id, RegisteredCurrency(it)) }
        }
    }
}

