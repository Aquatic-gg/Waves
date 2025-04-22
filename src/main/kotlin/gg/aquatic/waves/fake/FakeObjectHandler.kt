package gg.aquatic.waves.fake

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange
import gg.aquatic.waves.Waves
import gg.aquatic.waves.chunk.AsyncPlayerChunkLoadEvent
import gg.aquatic.waves.chunk.cache.ChunkCacheHandler
import gg.aquatic.waves.chunk.chunkId
import gg.aquatic.waves.fake.block.FakeBlock
import gg.aquatic.waves.fake.block.FakeBlockInteractEvent
import gg.aquatic.waves.fake.entity.FakeEntityInteractEvent
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.util.event.event
import gg.aquatic.waves.util.packetEvent
import gg.aquatic.waves.util.player
import gg.aquatic.waves.util.runAsync
import gg.aquatic.waves.util.runAsyncTimer
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.concurrent.ConcurrentHashMap

object FakeObjectHandler : WavesModule {
    override val type: WaveModules = WaveModules.FAKE_OBJECTS

    internal val tickableObjects = ConcurrentHashMap.newKeySet<FakeObject>()
    internal val idToEntity = ConcurrentHashMap<Int, EntityBased>()
    internal val locationToBlocks = ConcurrentHashMap<Location, MutableSet<FakeBlock>>()
    val objectRemovalQueue = ConcurrentHashMap.newKeySet<FakeObject>()

    override fun initialize(waves: Waves) {
        runAsyncTimer(
            100, 1
        ) {
            if (objectRemovalQueue.isNotEmpty()) {
                tickableObjects -= objectRemovalQueue
                objectRemovalQueue.clear()
            }
            for (tickableObject in tickableObjects) {
                tickableObject.handleTick()
            }
        }

        event<AsyncPlayerChunkLoadEvent> {
            val obj =
                ChunkCacheHandler.getObject(
                    it.chunk.x,
                    it.chunk.z,
                    it.player.world,
                    FakeObjectChunkBundle::class.java
                ) as? FakeObjectChunkBundle
                    ?: return@event
            tickableObjects += obj.blocks
            tickableObjects += obj.entities
            for (block in obj.blocks) {
                if (block.viewers.contains(it.player)) {
                    val index = (block.location.y.toInt() + 64) / 16
                    val chunk = it.wrappedPacket.column.chunks[index]
                    chunk.set(
                        block.location.x.toInt() and 0xf,
                        block.location.y.toInt() and 0xf,
                        block.location.z.toInt() and 0xf,
                        SpigotConversionUtil.fromBukkitBlockData(block.block.blockData)
                    )
                    block.isViewing += it.player
                }
            }
        }
        event<PlayerChunkUnloadEvent> {
            runAsync {
                for (tickableObject in tickableObjects) {
                    if (tickableObject.location.chunk != it.chunk) continue
                    handlePlayerRemove(it.player, tickableObject, false)
                }
            }
        }

        event<PlayerQuitEvent> {
            handlePlayerRemove(it.player)
        }
        event<PlayerJoinEvent> {
            for (tickableObject in tickableObjects) {
                if (tickableObject.audience.canBeApplied(it.player)) {
                    tickableObject.addViewer(it.player)
                }
            }
        }
        packetEvent<PacketSendEvent> {
            val player = player() ?: return@packetEvent
            if (packetType == PacketType.Play.Server.BLOCK_CHANGE) {
                val packet = WrapperPlayServerBlockChange(this)
                val blocks = locationToBlocks[player.world.getBlockAt(
                    packet.blockPosition.x,
                    packet.blockPosition.y,
                    packet.blockPosition.z
                ).location] ?: return@packetEvent

                for (block in blocks) {
                    if (block.viewers.contains(player)) {
                        if (!block.destroyed) {
                            val newState = SpigotConversionUtil.fromBukkitBlockData(block.block.blockData)
                            packet.blockState = newState
                            break
                        }
                    }
                }
            }
        }

        event<PlayerInteractEvent> {
            if (it.hand == EquipmentSlot.OFF_HAND) return@event
            val blocks = locationToBlocks[it.clickedBlock?.location ?: return@event] ?: return@event
            for (block in blocks) {
                if (block.viewers.contains(it.player)) {
                    it.isCancelled = true
                    val event = FakeBlockInteractEvent(
                        block,
                        it.player,
                        it.action == Action.LEFT_CLICK_BLOCK || it.action == Action.LEFT_CLICK_AIR
                    )
                    block.onInteract(event)
                    if (!block.destroyed) {
                        if (it.action == Action.RIGHT_CLICK_AIR || it.action == Action.RIGHT_CLICK_BLOCK) {
                            /*
                            runLaterSync(1) {
                                block.show(it.player)
                            }
                             */
                        } else {
                            block.show(it.player)
                        }
                    }
                    break
                }
            }

        }
        packetEvent<PacketReceiveEvent> {
            if (this.packetType != PacketType.Play.Client.INTERACT_ENTITY) return@packetEvent
            val packet = WrapperPlayClientInteractEntity(this)
            val entity = idToEntity[packet.entityId] ?: return@packetEvent
            val event = FakeEntityInteractEvent(
                entity,
                this.player() ?: return@packetEvent,
                (packet.action == WrapperPlayClientInteractEntity.InteractAction.ATTACK)
            )
            entity.onInteract(event)
        }
    }

    private fun handlePlayerRemove(player: Player) {
        for (tickableObject in tickableObjects) {
            handlePlayerRemove(player, tickableObject, true)
        }
    }

    internal fun handlePlayerRemove(player: Player, fakeObject: FakeObject, removeViewer: Boolean = false) {
        fakeObject.isViewing -= player
        if (removeViewer) {
            fakeObject.viewers -= player
        }

        /*
        if (fakeObject.location.chunk.trackedByPlayers().isEmpty() && fakeObject.isViewing.isEmpty()) {
            objectRemovalQueue += fakeObject
        }
         */
    }

    override fun disable(waves: Waves) {

    }
}