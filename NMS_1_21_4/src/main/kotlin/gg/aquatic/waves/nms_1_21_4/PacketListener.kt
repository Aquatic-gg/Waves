package gg.aquatic.waves.nms_1_21_4

import gg.aquatic.waves.api.event.call
import gg.aquatic.waves.api.event.packet.PacketChunkLoadEvent
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import org.bukkit.entity.Player

class PacketListener(
    val player: Player
): ChannelDuplexHandler() {

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        if (ctx !is Packet<*>) {
            super.write(ctx, msg, promise)
            return
        }

        when (ctx) {
            is ClientboundLevelChunkWithLightPacket -> {
                val event = PacketChunkLoadEvent(player, ctx.x, ctx.z)
                event.call()

                if (event.isCancelled) {
                    return
                }
            }
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {

    }
}