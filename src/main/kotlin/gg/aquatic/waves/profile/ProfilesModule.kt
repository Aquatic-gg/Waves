package gg.aquatic.waves.profile

import gg.aquatic.waves.data.DataDriver
import gg.aquatic.waves.data.MySqlDriver
import gg.aquatic.waves.Waves
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.profile.event.AsyncProfileLoadEvent
import gg.aquatic.waves.profile.event.ProfileUnloadEvent
import gg.aquatic.waves.profile.module.ProfileModule
import gg.aquatic.waves.sync.SyncHandler
import gg.aquatic.waves.sync.SyncedPlayer
import gg.aquatic.waves.api.event.call
import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.util.logger.type.InfoLogger
import gg.aquatic.waves.util.runAsync
import gg.aquatic.waves.util.toBytes
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.sql.Statement
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object ProfilesModule : WavesModule {
    override val type: WaveModules = WaveModules.PROFILES

    val driver: DataDriver
        get() {
            return Waves.INSTANCE.configValues.profilesDriver
        }

    val cache = ConcurrentHashMap<UUID, AquaticPlayer>()
    val playersSaving = HashSet<UUID>()
    val playersLoading = HashSet<UUID>()
    val playersAwaiting = HashSet<UUID>()
    val modules = HashMap<String, ProfileModule>()

    override fun initialize(waves: Waves) {
        if (driver is MySqlDriver) {
            driver.execute(
                "" +
                        "CREATE TABLE IF NOT EXISTS " +
                        "aquaticprofiles (" +
                        "id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                        "uuid BINARY(16) NOT NULL UNIQUE," +
                        "username NVARCHAR(64) NOT NULL" +
                        ")"

            ) {}
        } else {
            driver.execute(
                "" +
                        "CREATE TABLE IF NOT EXISTS " +
                        "aquaticprofiles (" +
                        "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                        "uuid BINARY(16) NOT NULL UNIQUE," +
                        "username NVARCHAR(64) NOT NULL" +
                        ")"

            ) {}
        }

        event<PlayerJoinEvent>(ignoredCancelled = true) {
            if (playersLoading.contains(it.player.uniqueId)) {
                return@event
            }
            playersLoading += it.player.uniqueId
            Bukkit.getConsoleSender().sendMessage("Loading profile!")

            fun loadPlayer(): AquaticPlayer {
                val player = getOrCreate(it.player)
                Bukkit.getConsoleSender().sendMessage("Profile Loaded!")
                AsyncProfileLoadEvent(player).call()
                cache[player.uuid] = player
                playersLoading -= it.player.uniqueId
                return player
            }

            val syncSettings = Waves.INSTANCE.configValues.syncSettings
            if (syncSettings.enabled) {
                runAsync {
                    val player = SyncHandler.client.getPlayerCache(it.player.uniqueId)
                    if (player != null) {
                        if (player.server == null) {
                            loadPlayer()
                        } else {
                            playersAwaiting += it.player.uniqueId
                        }
                    } else {
                        val aquaticPlayer = loadPlayer()
                        val cachedPlayer =
                            SyncedPlayer(
                                aquaticPlayer.uuid,
                                aquaticPlayer.username,
                                syncSettings.serverId,
                                HashMap()
                            )
                        SyncHandler.client.cachePlayer(cachedPlayer)
                    }
                }

            } else {
                runAsync {
                    loadPlayer()
                }
            }


        }
        event<PlayerQuitEvent>(ignoredCancelled = true) {
            Bukkit.getConsoleSender().sendMessage("Unloading profile!")
            val aPlayer = cache[it.player.uniqueId] ?: return@event
            playersSaving += it.player.uniqueId
            ProfileUnloadEvent(aPlayer).call()
            fun savePlayer() {
                save(aPlayer)
                playersSaving -= it.player.uniqueId
                cache.remove(it.player.uniqueId)
            }

            val syncSettings = Waves.INSTANCE.configValues.syncSettings
            if (syncSettings.enabled) {
                runAsync {
                    savePlayer()
                    val player = SyncHandler.client.getPlayerCache(it.player.uniqueId)
                    if (player != null) {
                        if (player.server == null) {
                            savePlayer()
                        } else {
                            playersAwaiting += it.player.uniqueId
                        }
                    }
                }

            } else {
                runAsync {
                    savePlayer()
                }
            }
        }
    }

    override fun disable(waves: Waves) {
        save(*cache.values.toTypedArray())
        cache.clear()
    }

    fun registerModule(module: ProfileModule) {
        if (modules.containsKey(module.id)) {
            return
        }
        modules[module.id] = module

        driver.useConnection {
            module.initialize(this)
        }
    }

    fun save(vararg players: AquaticPlayer) {
        driver.useConnection {
            for (player in players) {
                if (player.updated) {
                    prepareStatement("UPDATE aquaticprofiles SET username = ? WHERE id = ?").use { preparedStatement ->
                        preparedStatement.setString(1, player.username)
                        preparedStatement.setInt(2, player.index)
                        preparedStatement.execute()
                    }
                }
                try {
                    for (value in player.entries.values) {
                        value.save(this)
                    }
                } catch (ex: Exception) {
                    rollback()
                    ex.printStackTrace()
                }
            }
        }
    }

    fun getOrCreate(player: Player): AquaticPlayer {
        return getOrCreate(player.uniqueId, player.name)
    }

    fun getOrCreate(uuid: UUID, username: String): AquaticPlayer {
        if (cache.containsKey(uuid)) {
            return cache[uuid]!!
        }

        val optionalPlayer = driver.executeQuery("SELECT * FROM aquaticprofiles WHERE uuid = ?",
            {
                setBytes(1, uuid.toBytes())
            },
            {
                if (next()) {
                    InfoLogger.send("Player was found in the database!")
                    val id = getInt("id")
                    val player = AquaticPlayer(id, uuid, getString("username"))
                    if (player.username != username) {
                        player.username = username
                        player.updated = true
                    }


                    return@executeQuery Optional.of(player)
                }
                Optional.empty<AquaticPlayer>()
            }
        )
        optionalPlayer.ifPresent {
            for (value in modules.values) {
                val entry = value.loadEntry(it)
                it.entries[value.id] = entry
            }
        }
        return optionalPlayer.orElseGet {
            InfoLogger.send("Player was not found in the database!")
            driver.useConnection {
                prepareStatement(
                    "INSERT INTO aquaticprofiles (uuid, username) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                ).use { preparedStatement ->
                    preparedStatement.setBytes(1, uuid.toBytes())
                    preparedStatement.setString(2, username)
                    preparedStatement.executeUpdate()
                    val keys = preparedStatement.generatedKeys
                    keys.next()
                    val id = keys.getInt(1)

                    val player = AquaticPlayer(id, uuid, username)
                    player.updated = true
                    player
                }
            }
            /*
            driver.preparedStatement("INSERT INTO aquaticprofiles (uuid, username) VALUES (?, ?)") {
                setBytes(1, uuid.toBytes())
                setString(2, username)
                executeUpdate()
                val keys = generatedKeys
                keys.next()
                val id = keys.getInt(1)

                val player = AquaticPlayer(id, uuid, username)
                player.updated = true
                /*
                for ((_,value) in modules) {
                    val entry = value.loadEntry(player)
                    player.entries[value.id] = entry
                }
                 */
                player
            }
             */
        }
    }

    fun get(uuid: UUID): Optional<AquaticPlayer> {
        if (cache.containsKey(uuid)) {
            return Optional.of(cache[uuid]!!)
        }
        return driver.executeQuery("SELECT * FROM aquaticprofiles WHERE uuid = ?",
            {
                setBytes(1, uuid.toBytes())
            },
            {
                if (next()) {
                    val player = AquaticPlayer(getInt("id"), uuid, getString("username"))
                    cache[uuid] = player
                    Optional.of(player)
                } else {
                    Optional.of(cache[uuid]!!)
                }
            }
        )
    }

}