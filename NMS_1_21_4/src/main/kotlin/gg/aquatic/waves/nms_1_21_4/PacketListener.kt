package gg.aquatic.waves.nms_1_21_4

import gg.aquatic.waves.api.ReflectionUtils
import gg.aquatic.waves.api.event.call
import gg.aquatic.waves.api.event.packet.*
import gg.aquatic.waves.api.nms.ProtectedPacket
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.core.NonNullList
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PacketListener(
    val player: Player,
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

            is ClientboundContainerSetSlotPacket -> {
                val event = PacketContainerSetSlotEvent(
                    player,
                    msg.containerId,
                    msg.stateId,
                    CraftItemStack.asCraftMirror(msg.item)
                )
                event.call()
                if (event.isCancelled) {
                    return
                }

                val newPacket = ClientboundContainerSetSlotPacket(
                    msg.containerId,
                    msg.stateId,
                    msg.slot,
                    CraftItemStack.asNMSCopy(event.item)
                )
                super.write(ctx, newPacket, promise)
                return
            }

            is ClientboundContainerSetContentPacket -> {
                val event = PacketContainerContentEvent(
                    player,
                    msg.containerId,
                    msg.items.map { (CraftItemStack.asCraftMirror(it)) }.toMutableList(),
                    CraftItemStack.asCraftMirror(msg.carriedItem)
                )
                event.call()
                if (event.isCancelled) {
                    return
                }
                val newPacket = ClientboundContainerSetContentPacket(
                    msg.containerId,
                    msg.stateId,
                    NonNullList.create<net.minecraft.world.item.ItemStack>().apply {
                        addAll(event.contents.map { (CraftItemStack.asNMSCopy(it)) })
                    },
                    CraftItemStack.asNMSCopy(event.carriedItem)
                )
                super.write(ctx, newPacket, promise)
                return
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