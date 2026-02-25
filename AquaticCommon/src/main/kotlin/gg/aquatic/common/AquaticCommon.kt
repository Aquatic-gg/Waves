package gg.aquatic.common

import org.bukkit.plugin.java.JavaPlugin

object AquaticCommon {

    lateinit var plugin: JavaPlugin
    lateinit var miniMessage: MiniMessageResolver

}

fun initializeCommon(plugin: JavaPlugin, miniMessageResolver: MiniMessageResolver) {
    AquaticCommon.plugin = plugin
    AquaticCommon.miniMessage = miniMessageResolver
}