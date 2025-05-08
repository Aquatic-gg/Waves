package gg.aquatic.waves.nms_1_21_4

import gg.aquatic.waves.api.ReflectionUtils
import gg.aquatic.waves.api.event.call
import gg.aquatic.waves.api.event.packet.PacketBlockChangeEvent
import gg.aquatic.waves.api.event.packet.PacketChunkLoadEvent
import gg.aquatic.waves.api.event.packet.PacketContainerClickEvent
import gg.aquatic.waves.api.event.packet.PacketContainerCloseEvent
import gg.aquatic.waves.api.event.packet.PacketInteractEvent
import gg.aquatic.waves.api.nms.ProtectedPacket
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PacketListener(
    val player: Player
) : ChannelDuplexHandler() {

    private val blockUpdateBlockStateField =
        ReflectionUtils.getField("blockState", ClientboundBlockUpdatePacket::class.java)

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        if (msg is ProtectedPacket) {
            super.write(ctx, msg.packet, promise)
        }
        if (ctx !is Packet<*>) {
            super.write(ctx, msg, promise)
            return
        }

        when (msg) {
            is ClientboundLevelChunkWithLightPacket -> {
                val event = PacketChunkLoadEvent(player, msg.x, msg.z, msg)
                event.call()

                msg.chunkData

                if (event.isCancelled) {
                    return
                }
            }

            is ClientboundBlockUpdatePacket -> {
                val event = PacketBlockChangeEvent(
                    player,
                    msg.pos.x,
                    msg.pos.y,
                    msg.pos.z,
                    msg.blockState.createCraftBlockData()
                )
                event.call()
                if (event.isCancelled) {
                    return
                }

                blockUpdateBlockStateField.set(
                    msg,
                    (event.blockData as CraftBlockData).state
                )
            }
        }
        super.write(ctx, msg, promise)
    }

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg is ProtectedPacket) {
            super.channelRead(ctx, msg.packet)
            return
        }
        if (msg !is Packet<*>) {
            super.channelRead(ctx, msg)
            return
        }

        when (msg) {
            is ServerboundInteractPacket -> {
                val event = PacketInteractEvent(player, msg.isAttack, msg.isUsingSecondaryAction, msg.entityId)
                event.call()
                if (event.isCancelled) {
                    return
                }
            }

            is ServerboundContainerClosePacket -> {
                val event = PacketContainerCloseEvent(player)
                event.call()
                if (event.isCancelled) {
                    return
                }
            }

            is ServerboundContainerClickPacket -> {
                val event = PacketContainerClickEvent(
                    player,
                    msg.containerId,
                    msg.stateId,
                    msg.slotNum,
                    msg.buttonNum,
                    msg.clickType.ordinal,
                    CraftItemStack.asCraftMirror(msg.carriedItem),
                    msg.changedSlots.mapValues { (if (it.value.isEmpty) null else CraftItemStack.asCraftMirror(it.value)) as ItemStack? }
                )
                event.call()
                if (event.isCancelled) {
                    return
                }
            }
        }

        super.channelRead(ctx, msg)

    }
}