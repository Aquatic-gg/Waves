package gg.aquatic.common

import gg.aquatic.common.ticker.GlobalTicker
import org.bukkit.plugin.java.JavaPlugin

object AquaticCommon {

    lateinit var plugin: JavaPlugin

}

fun initializeCommon(plugin: JavaPlugin) {
    AquaticCommon.plugin = plugin
    GlobalTicker.initialize()
}