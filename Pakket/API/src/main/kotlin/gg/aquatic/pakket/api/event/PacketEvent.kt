package gg.aquatic.pakket.api.event

import gg.aquatic.kevent.Cancellable


abstract class PacketEvent : Cancellable {

    override var cancelled: Boolean = false

    fun then(then: () -> Unit) {
        thens.add(then)
    }

    val thens = ArrayList<() -> Unit>()

}