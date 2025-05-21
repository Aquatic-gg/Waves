package gg.aquatic.waves.nms_1_21_4

import gg.aquatic.waves.api.ReflectionUtils
import gg.aquatic.waves.api.event.call
import gg.aquatic.waves.api.event.packet.*
import gg.aquatic.waves.api.nms.ProtectedPacket
import gg.aquatic.waves.api.nms.meg.MEGPacketHandler
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.core.NonNullList
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.entity.CraftEntityType
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PacketListener(
    val player: Player,
) : ChannelDuplexHandler() {

    private val blockUpdateBlockStateField =
        ReflectionUtils.getField("blockState", ClientboundBlockUpdatePacket::class.java)

    override fun write(ctx: ChannelHandlerContext?, msg: Any, promise: ChannelPromise?) {
        var isMegPacket = false
        val packet = if (Bukkit.getPluginManager().getPlugin("ModelEngine") != null) {
            if (MEGPacketHandler.isMegPacket(msg)) {
                isMegPacket = true
                MEGPacketHandler.unpackPacket(msg)
            } else msg
        } else msg

        if (packet is ProtectedPacket) {
            super.write(ctx, packet.packet, promise)
            return
        }
        if (packet !is Packet<*>) {
            super.write(ctx, packet, promise)
            return
        }

        when (packet) {
            is ClientboundAddEntityPacket -> {
                val event = PacketEntitySpawnEvent(player,packet.id, packet.uuid, CraftEntityType.minecraftToBukkit(packet.type),
                    Location(player.world, packet.x, packet.y, packet.z, packet.yRot, packet.yRot))
                event.call()
                if (event.isCancelled) {
                    return
                }
                super.write(ctx, if (isMegPacket) msg else packet, promise)
                event.then()
                return
            }
            is ClientboundRemoveEntitiesPacket -> {
                val event = PacketDestroyEntitiesPacket(player,packet.entityIds.toIntArray())
                event.call()
                if (event.isCancelled) {
                    return
                }
                super.write(ctx, if (isMegPacket) msg else packet, promise)
                event.then()
                return
            }
            is ClientboundLevelChunkWithLightPacket -> {
                val event = PacketChunkLoadEvent(player, packet.x, packet.z, packet,packet.chunkData.extraPackets.toMutableList())
                event.call()

                if (event.isCancelled) {
                    return
                }
                packet.chunkData.extraPackets += (event.extraPackets.map { it -> it as Packet<*> }.toMutableList())
                super.write(ctx, packet, promise)
                event.then()
                return
            }

            is ClientboundBlockUpdatePacket -> {

                val event = PacketBlockChangeEvent(
                    player,
                    packet.pos.x,
                    packet.pos.y,
                    packet.pos.z,
                    packet.blockState.createCraftBlockData()
                )
                event.call()
                if (event.isCancelled) {
                    return
                }
                val newPacket = ClientboundBlockUpdatePacket(packet.pos, (event.blockData as CraftBlockData).state)
                super.write(ctx, newPacket, promise)
                event.then()
                return
            }

            is ClientboundContainerSetSlotPacket -> {
                val event = PacketContainerSetSlotEvent(
                    player,
                    packet.containerId,
                    packet.stateId,
                    CraftItemStack.asCraftMirror(packet.item)
                )
                event.call()
                if (event.isCancelled) {
                    return
                }

                val newPacket = ClientboundContainerSetSlotPacket(
                    packet.containerId,
                    packet.stateId,
                    packet.slot,
                    CraftItemStack.asNMSCopy(event.item)
                )
                super.write(ctx, newPacket, promise)
                event.then()
                return
            }

            is ClientboundContainerSetContentPacket -> {
                val event = PacketContainerContentEvent(
                    player,
                    packet.containerId,
                    packet.items.map { (CraftItemStack.asCraftMirror(it)) }.toMutableList(),
                    CraftItemStack.asCraftMirror(packet.carriedItem)
                )
                event.call()
                if (event.isCancelled) {
                    return
                }
                val newPacket = ClientboundContainerSetContentPacket(
                    packet.containerId,
                    packet.stateId,
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
                val event = PacketContainerOpenEvent(player, packet.containerId)
                event.call()
                if (event.isCancelled) {
                    return
                }
                super.write(ctx, packet, promise)
                event.then()
                return
            }
        }
        super.write(ctx, packet, promise)
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
                super.channelRead(ctx, msg)
                event.then()
                return
            }
        }

        super.channelRead(ctx, msg)

    }
}