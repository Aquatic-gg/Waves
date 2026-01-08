package gg.aquatic.pakket

import gg.aquatic.kevent.subscribe
import gg.aquatic.pakket.api.event.PacketEvent
import gg.aquatic.pakket.api.nms.NMSHandler
import gg.aquatic.pakket.nms_1_21_9.NMSHandlerImpl

object Pakket {

    val handler by lazy {
        when (NMSVersion.ofAquatic()) {
            NMSVersion.V_1_21_9 -> NMSHandlerImpl
            else -> throw IllegalStateException("Unsupported server version.")
        }
    }

}

inline fun <reified T: PacketEvent> packetEvent(crossinline block: (T) -> Unit) {
    NMSHandler.eventBus.subscribe<T> { block(it) }
}