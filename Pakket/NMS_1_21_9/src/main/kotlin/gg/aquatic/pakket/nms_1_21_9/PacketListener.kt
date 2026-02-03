package gg.aquatic.pakket.nms_1_21_9

import gg.aquatic.pakket.api.ReflectionUtils
import gg.aquatic.pakket.api.event.PacketEvent
import gg.aquatic.pakket.api.event.packet.PacketBlockChangeEvent
import gg.aquatic.pakket.api.event.packet.PacketChunkLoadEvent
import gg.aquatic.pakket.api.event.packet.PacketContainerClickEvent
import gg.aquatic.pakket.api.event.packet.PacketContainerCloseEvent
import gg.aquatic.pakket.api.event.packet.PacketContainerContentEvent
import gg.aquatic.pakket.api.event.packet.PacketContainerOpenEvent
import gg.aquatic.pakket.api.event.packet.PacketContainerSetSlotEvent
import gg.aquatic.pakket.api.event.packet.PacketDestroyEntitiesPacket
import gg.aquatic.pakket.api.event.packet.PacketEntitySpawnEvent
import gg.aquatic.pakket.api.event.packet.PacketInteractEvent
import gg.aquatic.pakket.api.event.packet.PacketItemRenameEvent
import gg.aquatic.pakket.api.event.packet.PacketRecipeBookChangeSettingsReceiveEvent
import gg.aquatic.pakket.api.event.packet.PacketRecipeBookSeenRecipeReceiveEvent
import gg.aquatic.pakket.api.nms.listener.IncomingPacketHandler
import gg.aquatic.pakket.api.nms.listener.OutgoingHandlerResult
import gg.aquatic.pakket.api.nms.listener.OutgoingPacketHandler
import gg.aquatic.pakket.api.nms.listener.PacketBundleAdapter
import gg.aquatic.pakket.api.nms.listener.PacketListenerBase
import net.minecraft.core.NonNullList
import net.minecraft.network.HashedStack
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.entity.CraftEntityType
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerRecipeBookSettingsChangeEvent
import org.bukkit.inventory.ItemStack

class PacketListener(player: Player) : PacketListenerBase(
    player = player,
    bundleAdapter = BundleAdapter,
    outgoingHandlers = listOf(OutgoingHandler),
    incomingHandlers = listOf(IncomingHandler)
) {

    private object BundleAdapter : PacketBundleAdapter {
        @Suppress("UNCHECKED_CAST")
        override fun unwrap(packet: Any): Iterable<Any>? {
            return if (packet is ClientboundBundlePacket) {
                packet.subPackets()
            } else {
                null
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun wrap(packets: List<Any>): Any {
            return ClientboundBundlePacket(packets.map { it as Packet<ClientGamePacketListener> })
        }
    }

    private object OutgoingHandler : OutgoingPacketHandler {
        override fun handle(packet: Any, player: Player): OutgoingHandlerResult? {
            return when (packet) {
                is ClientboundAddEntityPacket -> {
                    val event = PacketEntitySpawnEvent(
                        player,
                        packet.id,
                        packet.uuid,
                        CraftEntityType.minecraftToBukkit(packet.type),
                        Location(player.world, packet.x, packet.y, packet.z, packet.yRot, packet.yRot)
                    )
                    OutgoingHandlerResult.Forward(event) { packet }
                }

                is ClientboundRemoveEntitiesPacket -> {
                    val event = PacketDestroyEntitiesPacket(player, packet.entityIds.toIntArray())
                    OutgoingHandlerResult.Forward(event) { packet }
                }

                is ClientboundLevelChunkWithLightPacket -> {
                    val event = PacketChunkLoadEvent(player, packet.x, packet.z, packet, ArrayList())
                    OutgoingHandlerResult.Forward(event) { packet }
                }

                is ClientboundBlockUpdatePacket -> {
                    val event = PacketBlockChangeEvent(
                        player,
                        packet.pos.x,
                        packet.pos.y,
                        packet.pos.z,
                        packet.blockState.createCraftBlockData()
                    )
                    OutgoingHandlerResult.Forward(event) {
                        ClientboundBlockUpdatePacket(
                            packet.pos,
                            (event.blockData as CraftBlockData).state
                        )
                    }
                }

                is ClientboundContainerSetSlotPacket -> {
                    val event = PacketContainerSetSlotEvent(
                        player,
                        packet.containerId,
                        packet.stateId,
                        CraftItemStack.asCraftMirror(packet.item)
                    )
                    OutgoingHandlerResult.Forward(event) {
                        ClientboundContainerSetSlotPacket(
                            packet.containerId,
                            packet.stateId,
                            packet.slot,
                            CraftItemStack.asNMSCopy(event.item)
                        )
                    }
                }

                is ClientboundContainerSetContentPacket -> {
                    val event = PacketContainerContentEvent(
                        player,
                        packet.containerId,
                        packet.items.map { CraftItemStack.asCraftMirror(it) }.toMutableList(),
                        CraftItemStack.asCraftMirror(packet.carriedItem)
                    )
                    OutgoingHandlerResult.Forward(event) {
                        ClientboundContainerSetContentPacket(
                            packet.containerId,
                            packet.stateId,
                            NonNullList.create<net.minecraft.world.item.ItemStack>().apply {
                                addAll(event.contents.map { CraftItemStack.asNMSCopy(it) })
                            },
                            CraftItemStack.asNMSCopy(event.carriedItem)
                        )
                    }
                }

                is ClientboundOpenScreenPacket -> {
                    val event = PacketContainerOpenEvent(player, packet.containerId)
                    OutgoingHandlerResult.Forward(event) { packet }
                }

                is ClientboundContainerClosePacket -> {
                    val event = PacketContainerCloseEvent(player)
                    OutgoingHandlerResult.Forward(event) { packet }
                }

                else -> null
            }
        }
    }

    private object IncomingHandler : IncomingPacketHandler {
        private val interactActionField =
            ReflectionUtils.getField("action", ServerboundInteractPacket::class.java).apply {
                isAccessible = true
            }
        private val interactTypeMethod = ReflectionUtils.getMethod("getType", interactActionField.type).apply {
            isAccessible = true
        }

        override fun handle(packet: Any, player: Player): PacketEvent? {
            return when (packet) {
                is ServerboundInteractPacket -> {
                    val action = interactActionField.get(packet)
                    val actionType = interactTypeMethod.invoke(action) as Enum<*>
                    val actionTypeId = actionType.ordinal
                    PacketInteractEvent(
                        player,
                        packet.isAttack,
                        packet.isUsingSecondaryAction,
                        packet.entityId,
                        PacketInteractEvent.InteractType.entries[actionTypeId]
                    )
                }

                is ServerboundContainerClosePacket -> {
                    PacketContainerCloseEvent(player)
                }

                is ServerboundRenameItemPacket -> {
                    PacketItemRenameEvent(player, packet.name)
                }

                is ServerboundRecipeBookSeenRecipePacket -> {
                    PacketRecipeBookSeenRecipeReceiveEvent(player, packet.recipe.index)
                }

                is ServerboundRecipeBookChangeSettingsPacket -> {
                    PacketRecipeBookChangeSettingsReceiveEvent(
                        player,
                        PlayerRecipeBookSettingsChangeEvent.RecipeBookType.entries[packet.bookType.ordinal],
                        packet.isOpen,
                        packet.isFiltering
                    )
                }

                is ServerboundContainerClickPacket -> {
                    val carriedItem = (packet.carriedItem as? HashedStack.ActualItem)?.let { carried ->
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
                    PacketContainerClickEvent(
                        player,
                        packet.containerId,
                        packet.stateId,
                        packet.slotNum.toInt(),
                        packet.buttonNum.toInt(),
                        packet.clickType.ordinal,
                        carriedItem,
                        packet.changedSlots.mapValues { null as ItemStack? },
                    )
                }

                else -> null
            }
        }
    }
}
