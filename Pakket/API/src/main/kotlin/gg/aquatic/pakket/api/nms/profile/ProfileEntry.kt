package gg.aquatic.pakket.api.nms.profile

import net.kyori.adventure.text.Component
import org.bukkit.GameMode

class ProfileEntry(
    val userProfile: UserProfile,
    val listed: Boolean,
    val latency: Int,
    val gameMode: GameMode,
    val displayName: Component?,
    val showHat: Boolean,
    val listOrder: Int,
)