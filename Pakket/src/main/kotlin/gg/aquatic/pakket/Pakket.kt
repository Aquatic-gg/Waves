package gg.aquatic.pakket

import gg.aquatic.kevent.subscribe
import gg.aquatic.pakket.api.NMSVersion
import gg.aquatic.pakket.api.event.PacketEvent
import gg.aquatic.pakket.api.nms.NMSHandler
import gg.aquatic.pakket.nms_1_21_9.NMSHandlerImpl
import org.bukkit.entity.Player

object Pakket {

    val handler: NMSHandler by lazy {
        when (NMSVersion.ofAquatic()) {
            NMSVersion.V_1_21_9 -> NMSHandlerImpl
            else -> throw IllegalStateException("Unsupported server version.")
        }
    }

}

inline fun <reified T : PacketEvent> packetEvent(crossinline block: (T) -> Unit) {
    NMSHandler.eventBus.subscribe<T> { block(it) }
}

fun Player.sendPacket(packet: Any, silent: Boolean = false) = Pakket.handler.sendPacket(packet, silent, this)