package gg.aquatic.waves.util.version

import org.bukkit.plugin.java.JavaPlugin

enum class ServerVersion {

    V_1_21_1,
    V_1_21_4,
    V_1_21_5;

    fun isOlder(version: ServerVersion): Boolean {
        return this.ordinal < version.ordinal
    }
    fun isNewer(version: ServerVersion): Boolean {
        return this.ordinal > version.ordinal
    }
    fun isOlderOrEqual(version: ServerVersion): Boolean {
        return this.ordinal <= version.ordinal
    }
    fun isNewerOrEqual(version: ServerVersion): Boolean {
        return this.ordinal >= version.ordinal
    }

    companion object {
        fun ofAquatic(plugin: JavaPlugin): ServerVersion? {
            val version = plugin.server.bukkitVersion.split("-")[0]
            val split = version.split(".")

            /*
            val major = split[0].toInt()
            val minor = split[1].toInt()
            val patch = split.getOrNull(2)?.toInt() ?: 0
             */

            return when(version) {
                "1.21.1" -> V_1_21_1
                "1.21.4" -> V_1_21_4
                "1.21.5" -> V_1_21_5
                else -> null
            }
        }
    }
}