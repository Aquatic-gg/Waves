package gg.aquatic.waves.api.event

abstract class PacketEvent: CancellableAquaticEvent(true) {

    var then: () -> Unit = {}

}