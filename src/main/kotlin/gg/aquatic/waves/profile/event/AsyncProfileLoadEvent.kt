package gg.aquatic.waves.profile.event

import gg.aquatic.waves.profile.AquaticPlayer
import gg.aquatic.waves.api.event.AquaticEvent

class AsyncProfileLoadEvent(
    val profile: AquaticPlayer
): AquaticEvent(true)