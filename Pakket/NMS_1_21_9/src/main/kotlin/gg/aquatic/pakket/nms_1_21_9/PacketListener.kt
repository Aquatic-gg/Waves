package gg.aquatic.pakket.nms_1_21_9

import gg.aquatic.pakket.api.ReflectionUtils
import gg.aquatic.pakket.api.event.PacketEvent
import gg.aquatic.pakket.api.event.packet.*
import gg.aquatic.pakket.api.nms.NMSHandler
import gg.aquatic.pakket.api.nms.ProtectedPacket
import gg.aquatic.pakket.api.nms.meg.MEGPacketHandler
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.core.NonNullList
import net.minecraft.network.HashedStack
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import org.bukkit.*
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.entity.CraftEntityType
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PacketListener(
    val player: Player,
) : ChannelDuplexHandler() {

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

        val packets =
            if (packet is ClientboundBundlePacket) packet.subPackets() else listOf<Packet<in ClientGamePacketListener>>(
                packet as? Packet<ClientGamePacketListener> ?: return super.write(
                    ctx,
                    if (isMegPacket) msg else packet,
                    promise
                )
            )
        val newPackets = ArrayList<Packet<in ClientGamePacketListener>>()
        val thens = ArrayList<() -> Unit>()
        for (subPacket in packets) {
            val pair = handlePacket(subPacket) ?: continue
            val (resultPacket, resultEvent) = pair
            resultEvent?.let { thens.add(it.then) }
            newPackets.add(resultPacket)
        }
        if (newPackets.isEmpty()) {
            return
        }
        if (newPackets.size == 1) {
            super.write(ctx, if (isMegPacket) msg else newPackets[0], promise)
            thens.forEach { it() }
            return
        }

        super.write(ctx, if (isMegPacket) msg else ClientboundBundlePacket(newPackets), promise)
        thens.forEach { it() }
        return
    }
    fun handlePacket(packet: Packet<in ClientGamePacketListener>): Pair<Packet<in ClientGamePacketListener>, PacketEvent?>? {
        when (packet) {
            is ClientboundAddEntityPacket -> {
                val event = PacketEntitySpawnEvent(
                    player, packet.id, packet.uuid, CraftEntityType.minecraftToBukkit(packet.type),
                    Location(player.world, packet.x, packet.y, packet.z, packet.yRot, packet.yRot)
                )
                NMSHandler.eventBus.post(event)
                if (event.cancelled) {
                    return null
                }
                return packet to event
            }
            is ClientboundRemoveEntitiesPacket -> {
                val event = PacketDestroyEntitiesPacket(player, packet.entityIds.toIntArray())
                NMSHandler.eventBus.post(event)
                if (event.cancelled) {
                    return null
                }
                return packet to event
            }
            is ClientboundLevelChunkWithLightPacket -> {
                val event = PacketChunkLoadEvent(player, packet.x, packet.z, packet, ArrayList())
                NMSHandler.eventBus.post(event)

                if (event.cancelled) {
                    return null
                }
                //packet.extraPackets!!.addAll(event.extraPackets.map { it as Packet<*> })
                return packet to event
            }

            is ClientboundBlockUpdatePacket -> {

                val event = PacketBlockChangeEvent(
                    player,
                    packet.pos.x,
                    packet.pos.y,
                    packet.pos.z,
                    packet.blockState.createCraftBlockData()
                )
                NMSHandler.eventBus.post(event)
                if (event.cancelled) {
                    return null
                }
                val newPacket = ClientboundBlockUpdatePacket(packet.pos, (event.blockData as CraftBlockData).state)
                return newPacket to event
            }

            is ClientboundContainerSetSlotPacket -> {
                val event = PacketContainerSetSlotEvent(
                    player,
                    packet.containerId,
                    packet.stateId,
                    CraftItemStack.asCraftMirror(packet.item)
                )
                NMSHandler.eventBus.post(event)
                if (event.cancelled) {
                    return null
                }

                val newPacket = ClientboundContainerSetSlotPacket(
                    packet.containerId,
                    packet.stateId,
                    packet.slot,
                    CraftItemStack.asNMSCopy(event.item)
                )
                return newPacket to event
            }

            is ClientboundContainerSetContentPacket -> {
                val event = PacketContainerContentEvent(
                    player,
                    packet.containerId,
                    packet.items.map { CraftItemStack.asCraftMirror(it) }.toMutableList(),
                    CraftItemStack.asCraftMirror(packet.carriedItem)
                )
                NMSHandler.eventBus.post(event)
                if (event.cancelled) {
                    return null
                }
                val newPacket = ClientboundContainerSetContentPacket(
                    packet.containerId,
                    packet.stateId,
                    NonNullList.create<net.minecraft.world.item.ItemStack>().apply {
                        addAll(event.contents.map { CraftItemStack.asNMSCopy(it) })
                    },
                    CraftItemStack.asNMSCopy(event.carriedItem)
                )
                return newPacket to event
            }
            is ClientboundOpenScreenPacket -> {
                val event = PacketContainerOpenEvent(player, packet.containerId)
                NMSHandler.eventBus.post(event)
                if (event.cancelled) {
                    return null
                }
                return packet to event
            }

            is ClientboundContainerClosePacket -> {
                val event = PacketContainerCloseEvent(player)
                NMSHandler.eventBus.post(event)
                if (event.cancelled) {
                    return null
                }
                return packet to event
            }
        }
        return packet to null
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
                NMSHandler.eventBus.post(event)
                if (event.cancelled) {
                    return
                }
                super.channelRead(ctx, msg)
                event.then()
                return
            }

            is ServerboundContainerClosePacket -> {
                val event = PacketContainerCloseEvent(player)
                NMSHandler.eventBus.post(event)
                if (event.cancelled) {
                    return
                }
                super.channelRead(ctx, msg)
                event.then()
                return
            }

            is ServerboundContainerClickPacket -> {
                val carriedItem = (msg.carriedItem as? HashedStack.ActualItem)?.let { carried ->
                    val type = carried.item.registeredName
                    carried.components.addedComponents

                    var item = NamespacedKey.fromString(type)?.let { typeKey ->
                        Registry.ITEM.get(typeKey)
                    }?.createItemStack(carried.count)

                    if (item != null) {
                        if (item.type == Material.AIR) return@let null
                        val nmsItem = CraftItemStack.asNMSCopy(item)
                        nmsItem.applyComponents(nmsItem.components)
                        item = CraftItemStack.asBukkitCopy(nmsItem)
                    }
                    item
                }
                val event = PacketContainerClickEvent(
                    player,
                    msg.containerId,
                    msg.stateId,
                    msg.slotNum.toInt(),
                    msg.buttonNum.toInt(),
                    msg.clickType.ordinal,
                    carriedItem,
                    msg.changedSlots.mapValues { null as ItemStack? },
                )

                NMSHandler.eventBus.post(event)

                if (event.cancelled) {
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