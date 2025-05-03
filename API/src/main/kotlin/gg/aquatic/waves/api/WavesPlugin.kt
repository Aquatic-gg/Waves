package gg.aquatic.waves.api

import org.bukkit.plugin.java.JavaPlugin

abstract class WavesPlugin: JavaPlugin() {

    companion object {
        lateinit var INSTANCE: WavesPlugin
    }

}