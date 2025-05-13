package gg.aquatic.waves.nms_1_21_5

import gg.aquatic.waves.api.ReflectionUtils
import gg.aquatic.waves.api.event.call
import gg.aquatic.waves.api.event.packet.*
import gg.aquatic.waves.api.nms.ProtectedPacket
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.core.NonNullList
import net.minecraft.network.HashedPatchMap
import net.minecraft.network.HashedStack
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
            return
        }
        if (msg !is Packet<*>) {
            super.write(ctx, msg, promise)
            return
        }

        when (msg) {
            is ClientboundLevelChunkWithLightPacket -> {
                val event = PacketChunkLoadEvent(player, msg.x, msg.z, msg,msg.chunkData.extraPackets.toMutableList())
                event.call()

                if (event.isCancelled) {
                    return
                }
                msg.chunkData.extraPackets += (event.extraPackets.map { it -> it as Packet<*> }.toMutableList())
                super.write(ctx, msg, promise)
                event.then()
                return
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
                val newPacket = ClientboundBlockUpdatePacket(msg.pos, (event.blockData as CraftBlockData).state)
                super.write(ctx, newPacket, promise)
                event.then()
                return
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
                event.then()
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
                event.then()
                return
            }
            is ClientboundOpenScreenPacket -> {
                val event = PacketContainerOpenEvent(player, msg.containerId)
                event.call()
                if (event.isCancelled) {
                    return
                }
                super.write(ctx, msg, promise)
                event.then()
                return
            }
        }
        super.write(ctx, msg, promise)
    }

    private val interactActionField = ReflectionUtils.getField("action", ServerboundInteractPacket::class.java).apply {
        isAccessible = true
    }
    private val interactTypeMethod = ReflectionUtils.getMethod("getType", interactActionField.type).apply {
        isAccessible = true
    }

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg is ProtectedPacket) {
            super.channelRead(ctx, msg.packet)
            return
        }

        when (msg) {
            is ServerboundInteractPacket -> {
                val action = interactActionField.get(msg)
                val actionType = interactTypeMethod.invoke(action) as Enum<*>
                val actionTypeId = actionType.ordinal

                val event = PacketInteractEvent(
                    player,
                    msg.isAttack,
                    msg.isUsingSecondaryAction,
                    msg.entityId,
                    PacketInteractEvent.InteractType.entries[actionTypeId]
                )
                event.call()
                if (event.isCancelled) {
                    return
                }
                super.channelRead(ctx, msg)
                event.then()
                return
            }

            is ServerboundContainerClosePacket -> {
                val event = PacketContainerCloseEvent(player)
                event.call()
                if (event.isCancelled) {
                    return
                }
                super.channelRead(ctx, msg)
                event.then()
                return
            }

            is ServerboundContainerClickPacket -> {
                val event = PacketContainerClickEvent(
                    player,
                    msg.containerId,
                    msg.stateId,
                    msg.slotNum.toInt(),
                    msg.buttonNum.toInt(),
                    msg.clickType.ordinal,
                    null,
                    msg.changedSlots.mapValues { null as ItemStack? },
                )

                event.call()
                if (event.isCancelled) {
                    return
                }
                super.channelRead(ctx, msg)
                event.then()
                return
            }
        }

        super.channelRead(ctx, msg)

    }
}