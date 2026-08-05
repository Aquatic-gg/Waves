package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

data class PacketBlockChangeEvent(
    val player: Player,
    val x: Int,
    val y: Int,
    val z: Int,
    var blockData: BlockData
): PacketEvent()