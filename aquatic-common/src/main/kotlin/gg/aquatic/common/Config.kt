package gg.aquatic.common

import gg.aquatic.common.coroutine.VirtualsCtx
import kotlinx.coroutines.withContext
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Config {
    private var file: File
    private var main: JavaPlugin

    constructor(file: File, main: JavaPlugin) {
        this.main = main
        this.file = file
    }
    constructor(path: String, main: JavaPlugin) {
        this.main = main
        file = File(main.dataFolder, path)
    }
    private val _config: FileConfiguration = YamlConfiguration()

    suspend fun load() = withContext(VirtualsCtx) {
        loadSync()
    }

    fun loadSync(): FileConfiguration {
        if (!file.exists()) {
            val resourcePath = file.absolutePath
                .removePrefix(main.dataFolder.absolutePath)
                .replace('\\', '/')
                .trim('/')

            if (main.getResource(resourcePath) != null) {
                main.saveResource(resourcePath, false)
            } else {
                file.parentFile?.mkdirs()
                runCatching { file.createNewFile() }
                    .onFailure { it.printStackTrace() }
            }
        }
        _config.load(file)
        return configuration
    }

    val configuration: FileConfiguration
        get() = _config

    @Suppress("unused")
    suspend fun save() = withContext(VirtualsCtx) {
        saveSync()
    }

    fun saveSync() {
        runCatching {
            configuration.save(file)
        }.onFailure { it.printStackTrace() }
    }

    @Suppress("unused")
    fun getFile(): File {
        return file
    }
}