package gg.aquatic.waves

import com.tcoded.folialib.FoliaLib
import gg.aquatic.waves.api.nms.NMSHandler
import gg.aquatic.waves.api.WavesPlugin
import gg.aquatic.waves.api.event.call
import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.command.AquaticBaseCommand
import gg.aquatic.waves.command.impl.GeneratePackCommand
import gg.aquatic.waves.command.impl.ItemConvertCommand
import gg.aquatic.waves.command.register
import gg.aquatic.waves.data.MySqlDriver
import gg.aquatic.waves.data.SQLiteDriver
import gg.aquatic.waves.fake.FakeObjectHandler
import gg.aquatic.waves.hologram.HologramHandler
import gg.aquatic.waves.input.InputModule
import gg.aquatic.waves.interactable.InteractableHandler
import gg.aquatic.waves.item.ItemHandler
import gg.aquatic.waves.menu.MenuHandler
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.nms_1_21_4.NMSHandlerImpl
import gg.aquatic.waves.pack.PackHandler
import gg.aquatic.waves.profile.ProfilesModule
import gg.aquatic.waves.sync.SyncHandler
import gg.aquatic.waves.sync.SyncSettings
import gg.aquatic.waves.util.Config
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.io.File

class Waves : WavesPlugin() {

    val modules = hashMapOf(
        WaveModules.PROFILES to ProfilesModule,
        WaveModules.ITEMS to ItemHandler,
        WaveModules.FAKE_OBJECTS to FakeObjectHandler,
        WaveModules.INTERACTABLES to InteractableHandler,
        WaveModules.INVENTORIES to gg.aquatic.waves.inventory.InventoryManager,
        WaveModules.MENUS to MenuHandler,
        WaveModules.HOLOGRAMS to HologramHandler,
        WaveModules.INPUT to InputModule
    )
    lateinit var configValues: WavesConfig

    /**
     * Indicates whether the `Waves` plugin has been fully initialized.
     * This variable is set to `true` when the plugin completes the initialization process
     * during the execution of the `onEnable` method. It is used to prevent certain actions
     * from being performed before the initialization is complete.
     *
     * This property is private to ensure controlled modification and only allows
     * being updated within the class.
     */
    var initialized = false
        private set

    companion object {
        val INSTANCE: Waves
            get() {
                return WavesPlugin.INSTANCE as Waves
            }

        fun getModule(type: WaveModules): WavesModule? {
            return INSTANCE.modules[type]
        }

        lateinit var NMS_HANDLER: NMSHandler
    }

    lateinit var foliaLib: FoliaLib

    override fun onLoad() {
        WavesPlugin.INSTANCE = this
        NMS_HANDLER = when (server.bukkitVersion) {
            "1.21.1-R0.1-SNAPSHOT" -> {
                gg.aquatic.waves.nms_1_21_1.NMSHandlerImpl
            }

            "1.21.4-R0.1-SNAPSHOT" -> {
                NMSHandlerImpl
            }

            else -> {
                gg.aquatic.waves.nms_1_21_5.NMSHandlerImpl
            }
        }

        foliaLib = FoliaLib(this)

        loadConfig()
    }

    override fun onEnable() {
        for ((_, module) in modules) {
            module.initialize(this@Waves)
        }
        PackHandler.loadPack()

        initialized = true
        WavesInitializeEvent().call()

        AquaticBaseCommand(
            "waves", "Waves base command", mutableListOf(),
            mutableMapOf(
                "itemconvert" to ItemConvertCommand,
                "generatepack" to GeneratePackCommand
            ), listOf()
        ).register("waves")

        event<PlayerJoinEvent> {
            NMS_HANDLER.injectPacketListener(it.player)
        }
        event<PlayerQuitEvent> {
            NMS_HANDLER.unregisterPacketListener(it.player)
        }
    }

    override fun onDisable() {
        ProfilesModule.save(*ProfilesModule.cache.values.toTypedArray())
    }

    fun loadConfig() {
        dataFolder.mkdirs()
        val config = Config("config.yml", this)
        config.load()

        val cfg = config.getConfiguration()!!
        val type = cfg.getString("databases.profiles.type", "SQLITE")!!
        val ip = cfg.getString("databases.profiles.ip", "")!!
        val port = cfg.getInt("databases.profiles.port", 3306)
        val userName = cfg.getString("databases.profiles.username", "")!!
        val password = cfg.getString("databases.profiles.password", "")!!
        val database = cfg.getString("databases.profiles.database", "")!!
        val maxPoolSize = cfg.getInt("databases.profiles.maxPoolSize", 10)
        val poolName = cfg.getString("databases.profiles.poolName", "Waves Hikari Pool")!!

        val driver = if (type.uppercase() == "SQLITE") {
            val file = File(dataFolder, "$database.db")
            file.createNewFile()
            SQLiteDriver(file)
        } else {
            MySqlDriver(ip, port, database, userName, password, maxPoolSize, poolName)
        }

        val syncEnabled = cfg.getBoolean("sync.enabled", false)
        val syncIP = cfg.getString("sync.ip", "localhost")!!
        val syncPort = cfg.getInt("sync.port", 8080)
        val syncPassword = cfg.getString("sync.protection-key", "<PASSWORD>")!!
        val syncServerId = cfg.getString("sync.server-id", "main")!!
        val syncSettings = SyncSettings(syncEnabled, syncIP, syncPort, syncPassword, syncServerId)

        configValues = WavesConfig(driver, syncSettings)
        if (syncEnabled) {
            SyncHandler.initializeClient(syncSettings)
        }
    }

}