package gg.aquatic.waves

import com.github.retrooper.packetevents.PacketEvents
import com.tcoded.folialib.FoliaLib
import gg.aquatic.waves.data.MySqlDriver
import gg.aquatic.waves.data.SQLiteDriver
import gg.aquatic.waves.chunk.ChunkTracker
import gg.aquatic.waves.command.AquaticBaseCommand
import gg.aquatic.waves.command.impl.GeneratePackCommand
import gg.aquatic.waves.command.impl.ItemConvertCommand
import gg.aquatic.waves.command.register
import gg.aquatic.waves.entity.EntityHandler
import gg.aquatic.waves.fake.FakeObjectHandler
import gg.aquatic.waves.hologram.HologramHandler
import gg.aquatic.waves.input.InputModule
import gg.aquatic.waves.interactable.InteractableHandler
import gg.aquatic.waves.item.ItemHandler
import gg.aquatic.waves.menu.MenuHandler
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.pack.PackHandler
import gg.aquatic.waves.profile.ProfilesModule
import gg.aquatic.waves.sync.SyncHandler
import gg.aquatic.waves.sync.SyncSettings
import gg.aquatic.waves.util.Config
import gg.aquatic.waves.util.event.call
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Waves : JavaPlugin() {

    val modules = hashMapOf(
        WaveModules.PROFILES to ProfilesModule,
        WaveModules.ITEMS to ItemHandler,
        WaveModules.ENTITIES to EntityHandler,
        WaveModules.FAKE_OBJECTS to FakeObjectHandler,
        WaveModules.CHUNK_TRACKER to ChunkTracker,
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
        lateinit var INSTANCE: Waves
            private set

        fun getModule(type: WaveModules): WavesModule? {
            return INSTANCE.modules[type]
        }
    }

    lateinit var foliaLib: FoliaLib

    override fun onLoad() {
        INSTANCE = this
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().load()

        foliaLib = FoliaLib(this)

        loadConfig()
    }

    override fun onEnable() {
        PacketEvents.getAPI().init()
        for ((_, module) in modules) {
            module.initialize(this@Waves)
        }
        PackHandler.loadPack()

        initialized = true
        WavesInitializeEvent().call()

        /*
        event<AsyncPlayerChatEvent> {
            runSync {
                val fakeEntity = FakeEntity(
                    EntityTypes.TEXT_DISPLAY,
                    it.player.location,
                    50,
                    GlobalAudience(),
                    {
                        val builder = EntityDataBuilder.TEXT_DISPLAY
                        builder.setText(Component.text("Example!"))
                        builder.setTranslation(0,1,0)
                        builder.setBillboard(Display.Billboard.VERTICAL)
                        this.entityData += builder.build().mapPair { it.index to it }
                    },
                    {},
                    {
                    }
                )
                fakeEntity.onUpdate = {
                    val ridePacket = WrapperPlayServerSetPassengers(
                        it.player!!.entityId,
                        listOf(fakeEntity.entityId).toIntArray()
                    )
                    it.player!!.toUser().sendPacket(ridePacket)
                }
                fakeEntity.addViewer(it.player)
                playerPair = it.player to fakeEntity
            }
        }
        runSyncTimer(1,1) {
            val pair = playerPair ?: return@runSyncTimer
            val player = pair.first
            val entity = pair.second

            //entity.teleport(player.location)
            entity.location = player.location
            entity.onUpdate(player)
        }
         */
        AquaticBaseCommand("waves", "Waves base command", mutableListOf(),
            mutableMapOf(
                "itemconvert" to ItemConvertCommand,
                "generatepack" to GeneratePackCommand
            ), listOf()).register("waves")
    }

    override fun onDisable() {
        ProfilesModule.save(*ProfilesModule.cache.values.toTypedArray())
        PacketEvents.getAPI().terminate()
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