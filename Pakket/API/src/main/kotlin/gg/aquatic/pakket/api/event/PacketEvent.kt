package gg.aquatic.pakket.api.event

import gg.aquatic.kevent.Cancellable


abstract class PacketEvent: Cancellable {

    override var cancelled: Boolean = false

    var then: () -> Unit = {}

}