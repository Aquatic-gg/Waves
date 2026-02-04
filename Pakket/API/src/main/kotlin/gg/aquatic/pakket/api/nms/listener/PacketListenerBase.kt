package gg.aquatic.pakket.api.nms.listener

import gg.aquatic.pakket.api.event.PacketEvent
import gg.aquatic.pakket.api.nms.NMSHandler
import gg.aquatic.pakket.api.nms.ProtectedPacket
import gg.aquatic.pakket.api.nms.meg.MEGPacketHandler
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import org.bukkit.Bukkit
import org.bukkit.entity.Player

abstract class PacketListenerBase(
    protected val player: Player,
    private val bundleAdapter: PacketBundleAdapter,
    private val outgoingHandlers: List<OutgoingPacketHandler>,
    private val incomingHandlers: List<IncomingPacketHandler>,
) : ChannelDuplexHandler() {

    override fun write(ctx: ChannelHandlerContext?, msg: Any, promise: ChannelPromise?) {
        val (isMegPacket, packet) = unwrapMegPacket(msg)

        if (packet is ProtectedPacket) {
            super.write(ctx, packet.packet, promise)
            return
        }

        val packets = bundleAdapter.unwrap(packet) ?: listOf(packet)
        val newPackets = ArrayList<Any>(packets.count())
        val thens = ArrayList<() -> Unit>()

        for (subPacket in packets) {
            val result = handleOutgoing(subPacket)
            if (result.packet == null) {
                continue
            }
            newPackets.add(result.packet)
            result.event?.let { thens.addAll(it.thens) }
        }

        if (newPackets.isEmpty()) {
            return
        }

        val toSend = if (newPackets.size == 1) {
            newPackets[0]
        } else {
            bundleAdapter.wrap(newPackets) ?: return
        }

        super.write(ctx, if (isMegPacket) msg else toSend, promise)
        thens.forEach { it() }
    }

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg is ProtectedPacket) {
            super.channelRead(ctx, msg.packet)
            return
        }

        if (msg != null) {
            for (handler in incomingHandlers) {
                val event = handler.handle(msg, player) ?: continue
                NMSHandler.eventBus.post(event)
                if (event.cancelled) {
                    return
                }
                super.channelRead(ctx, msg)
                event.thens.forEach { it() }
                return
            }
        }

        super.channelRead(ctx, msg)
    }

    private data class OutgoingDispatchResult(val packet: Any?, val event: PacketEvent?)

    private fun handleOutgoing(packet: Any): OutgoingDispatchResult {
        for (handler in outgoingHandlers) {
            val result = handler.handle(packet, player) ?: continue
            val event = when (result) {
                is OutgoingHandlerResult.Drop -> result.event
                is OutgoingHandlerResult.Forward -> result.event
            }
            event?.let {
                NMSHandler.eventBus.post(it)
                if (it.cancelled) {
                    return OutgoingDispatchResult(null, null)
                }
            }
            return when (result) {
                is OutgoingHandlerResult.Drop -> OutgoingDispatchResult(null, event)
                is OutgoingHandlerResult.Forward ->
                    OutgoingDispatchResult(result.packetFactory(), event)
            }
        }
        return OutgoingDispatchResult(packet, null)
    }

    private fun unwrapMegPacket(msg: Any): Pair<Boolean, Any> {
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") != null) {
            if (MEGPacketHandler.isMegPacket(msg)) {
                return true to MEGPacketHandler.unpackPacket(msg)
            }
        }
        return false to msg
    }
}
