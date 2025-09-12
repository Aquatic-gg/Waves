package gg.aquatic.waves.fake.entity

import gg.aquatic.waves.fake.EntityBased
import gg.aquatic.waves.api.event.AquaticEvent
import org.bukkit.entity.Player

class FakeEntityInteractEvent(
    val fakeEntity: EntityBased,
    val player: Player,
    val isLeftClick: Boolean
): AquaticEvent()