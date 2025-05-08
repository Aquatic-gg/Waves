package gg.aquatic.waves.fake

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import gg.aquatic.waves.Waves
import gg.aquatic.waves.chunk.cache.ChunkCacheHandler
import gg.aquatic.waves.fake.block.FakeBlock
import gg.aquatic.waves.fake.block.FakeBlockInteractEvent
import gg.aquatic.waves.fake.entity.FakeEntityInteractEvent
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.api.event.packet.PacketBlockChangeEvent
import gg.aquatic.waves.api.event.packet.PacketChunkLoadEvent
import gg.aquatic.waves.api.event.packet.PacketInteractEvent
import gg.aquatic.waves.util.runAsync
import gg.aquatic.waves.util.runAsyncTimer
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

        event<PacketChunkLoadEvent> {
            val obj =
                ChunkCacheHandler.getObject(
                    it.x,
                    it.z,
                    it.player.world,
                    FakeObjectChunkBundle::class.java
                ) as? FakeObjectChunkBundle
                    ?: return@event
            tickableObjects += obj.blocks
            tickableObjects += obj.entities

            val packet = it.packet
            Waves.NMS_HANDLER.modifyChunkPacketBlocks(it.player.world, packet) { sections ->
                for (block in obj.blocks) {
                    if (block.viewers.contains(it.player)) {
                        val index = (block.location.y.toInt() + 64) / 16
                        val section = sections[index]
                        section.set(
                            block.location.x.toInt() and 0xf,
                            block.location.y.toInt() and 0xf,
                            block.location.z.toInt() and 0xf,
                            block.block.blockData
                        )
                        block.isViewing += it.player
                    }
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
        event<PacketBlockChangeEvent> {
            val player = it.player
            val blocks = locationToBlocks[player.world.getBlockAt(
                it.x,
                it.y,
                it.z
            ).location] ?: return@event
            for (block in blocks) {
                if (block.viewers.contains(player)) {
                    if (!block.destroyed) {
                        val newState = block.block.blockData
                        it.blockData = newState
                        break
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
        event<PacketInteractEvent> {
            val entity = idToEntity[it.entityId] ?: return@event
            val event = FakeEntityInteractEvent(
                entity,
                it.player,
                it.isAttack
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