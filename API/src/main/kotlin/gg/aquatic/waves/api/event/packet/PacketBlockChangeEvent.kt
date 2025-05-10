package gg.aquatic.waves.api.event.packet

import gg.aquatic.waves.api.event.PacketEvent
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

class PacketBlockChangeEvent(
    val player: Player,
    val x: Int,
    val y: Int,
    val z: Int,
    var blockData: BlockData
): PacketEvent() {
}