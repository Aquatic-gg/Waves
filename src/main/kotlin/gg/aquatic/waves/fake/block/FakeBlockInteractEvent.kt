package gg.aquatic.waves.fake.block

import gg.aquatic.waves.api.event.AquaticEvent
import org.bukkit.entity.Player

class FakeBlockInteractEvent(
    val fakeBlock: FakeBlock,
    val player: Player,
    val isLeftClick: Boolean
): AquaticEvent() {
}