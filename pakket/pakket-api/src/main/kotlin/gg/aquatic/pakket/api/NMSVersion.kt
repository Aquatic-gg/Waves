package gg.aquatic.pakket.api

import org.bukkit.Bukkit

enum class NMSVersion {

    V_1_21_1,
    V_1_21_4,
    V_1_21_5,
    V_1_21_7,
    V_1_21_9,
    V_26_1_1,
    V_26_2;

    companion object {
        fun ofAquatic(): NMSVersion? {
            val version = Bukkit.getServer().bukkitVersion.substringBefore("-")

            // Since 26.x the version scheme is <major>.<minor>.build.<number> (e.g. 26.2.build.121-stable),
            // so only the major.minor part is meaningful for matching.
            val parts = version.split(".")
            val normalized = if ((parts.firstOrNull()?.toIntOrNull() ?: 0) >= 26) {
                parts.take(2).joinToString(".")
            } else {
                version
            }

            return when(normalized) {
                "1.21.1" -> V_1_21_1
                "1.21.4" -> V_1_21_4
                "1.21.5" -> V_1_21_5
                "1.21.6", "1.21.7", "1.21.8" -> V_1_21_7
                "1.21.9", "1.21.10", "1.21.11" -> V_1_21_9
                "26.1" -> V_26_1_1
                "26.2" -> V_26_2
                else -> null
            }
        }
    }
}
