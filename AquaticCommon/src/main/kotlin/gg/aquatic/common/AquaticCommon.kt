package gg.aquatic.common

import org.bukkit.plugin.java.JavaPlugin

object AquaticCommon {

    lateinit var plugin: JavaPlugin

}

fun initializeCommon(plugin: JavaPlugin) {
    AquaticCommon.plugin = plugin
}