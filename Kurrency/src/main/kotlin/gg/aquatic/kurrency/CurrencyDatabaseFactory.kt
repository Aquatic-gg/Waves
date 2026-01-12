package gg.aquatic.kurrency

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import gg.aquatic.kurrency.db.BalancesTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object CurrencyDatabaseFactory {

    fun init(url: String, driver: String, user: String, pass: String): Database {
        val config = HikariConfig().apply {
            jdbcUrl = url
            driverClassName = driver
            username = user
            password = pass

            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

            connectionTimeout = 3000
            idleTimeout = 600000
            maxLifetime = 1800000

            // Optimization for MySQL/MariaDB
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }

        val dataSource = HikariDataSource(config)
        val db = Database.connect(dataSource)

        transaction(db) {
            SchemaUtils.create(BalancesTable)
        }

        return db
    }
}