package gg.aquatic.waves.profile

import gg.aquatic.waves.profile.module.ProfileModuleEntry
import org.bukkit.entity.Player
import java.util.UUID

class AquaticPlayer(
    val index: Int,
    val uuid: UUID,
    var username: String
) {

    var updated = false

    val entries = HashMap<String,ProfileModuleEntry>()
}

fun Player.toAquaticPlayer(): AquaticPlayer? {
    val profilesModule =
}