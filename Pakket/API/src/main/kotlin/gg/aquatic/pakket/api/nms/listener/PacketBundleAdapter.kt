package gg.aquatic.pakket.api.nms.listener

interface PacketBundleAdapter {
    fun unwrap(packet: Any): Iterable<Any>?
    fun wrap(packets: List<Any>): Any?
}