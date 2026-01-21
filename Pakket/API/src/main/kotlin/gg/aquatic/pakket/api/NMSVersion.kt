package gg.aquatic.pakket.api

import org.bukkit.Bukkit

enum class NMSVersion {

    V_1_21_1,
    V_1_21_4,
    V_1_21_5,
    V_1_21_7,
    V_1_21_9;

    companion object {
        fun ofAquatic(): NMSVersion? {
            val version = Bukkit.getServer().bukkitVersion.substringBefore("-")

            return when(version) {
                "1.21.1" -> V_1_21_1
                "1.21.4" -> V_1_21_4
                "1.21.5" -> V_1_21_5
                "1.21.6", "1.21.7", "1.21.8" -> V_1_21_7
                "1.21.9", "1.21.10", "1.21.11" -> V_1_21_9
                else -> null
            }
        }
    }
}