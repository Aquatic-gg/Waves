package gg.aquatic.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object HikariDBFactory {
    fun createDataSource(url: String, driver: String, user: String, pass: String): HikariDataSource {
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

        return HikariDataSource(config)
    }

    fun init(url: String, driver: String, user: String, pass: String, vararg tables: Table): Database {
        val dataSource = createDataSource(url, driver, user, pass)
        val db = Database.connect(dataSource)

        transaction(db) {
            SchemaUtils.create(*tables)
        }

        return db
    }
}
