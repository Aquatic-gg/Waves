package gg.aquatic.clientside

import gg.aquatic.common.event
import gg.aquatic.common.ticker.Ticker
import gg.aquatic.pakket.api.event.packet.PacketBlockChangeEvent
import gg.aquatic.pakket.api.event.packet.PacketChunkLoadEvent
import gg.aquatic.pakket.api.event.packet.PacketInteractEvent
import gg.aquatic.pakket.packetEvent
import gg.aquatic.clientside.block.FakeBlock
import gg.aquatic.clientside.entity.FakeEntity
import gg.aquatic.clientside.meg.MEGInteractableHandler
import gg.aquatic.common.ChunkId
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.concurrent.ConcurrentHashMap

object FakeObjectHandler {

    internal val tickableObjects = ConcurrentHashMap.newKeySet<FakeObject>()
    internal val idToEntity = ConcurrentHashMap<Int, FakeEntity>()
    internal val locationToBlocks = ConcurrentHashMap<Location, MutableSet<FakeBlock>>()
    private val objectRemovalQueue: MutableSet<FakeObject> = ConcurrentHashMap.newKeySet()

    private val chunkCache = ConcurrentHashMap<String,MutableMap<ChunkId, ChunkBundle>>()

    private var tickCycle = 0

    fun initialize() {
        setupTicker()
        setupModelEngine()
        setupChunkEvents()
        setupPlayerEvents()
        setupInteractionEvents()
    }

    private fun setupTicker() {
        Ticker {
            tickCycle = (tickCycle + 1) % 4

            if (objectRemovalQueue.isNotEmpty()) {
                tickableObjects.removeAll(objectRemovalQueue)
                objectRemovalQueue.clear()
            }

            for (tickableObject in tickableObjects) {
                if (tickableObject.destroyed) {
                    objectRemovalQueue.add(tickableObject)
                    continue
                }
                tickableObject.handleTick(tickCycle)
            }
        }.register()
    }

    private fun setupModelEngine() {
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") != null) {
            MEGInteractableHandler()
        }
    }

    private fun setupChunkEvents() {
        packetEvent<PacketChunkLoadEvent> {
            val bundle = getChunkCacheBundle(it.x, it.z, it.player.world) ?: return@packetEvent
            it.then {
                for (block in bundle.blocks) block.updateVisibility(it.player)
                for (entity in bundle.entities) entity.updateVisibility(it.player)
            }
        }
        event<PlayerChunkUnloadEvent> {
            val bundle = getChunkCacheBundle(it.chunk.x, it.chunk.z, it.world) ?: return@event
            for (block in bundle.blocks) block.updateVisibility(it.player)
            for (entity in bundle.entities) entity.updateVisibility(it.player)
        }
    }

    private fun setupPlayerEvents() {
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
    }

    private fun setupInteractionEvents() {
        packetEvent<PacketBlockChangeEvent> {
            val player = it.player
            val blocks = locationToBlocks[Location(
                player.world,
                it.x.toDouble(),
                it.y.toDouble(),
                it.z.toDouble()
            ).toBlockLocation()] ?: return@packetEvent

            for (block in blocks) {
                if (block.isAudienceMember(player) && !block.destroyed) {
                    it.blockData = block.block.blockData
                    break
                }
            }
        }

        event<PlayerInteractEvent> {
            if (it.hand == EquipmentSlot.OFF_HAND) return@event
            val blocks = locationToBlocks[it.clickedBlock?.location ?: return@event] ?: return@event
            for (block in blocks) {
                if (block.destroyed) continue
                if (block.isAudienceMember(it.player)) {
                    it.isCancelled = true
                    val isLeft = it.action == Action.LEFT_CLICK_BLOCK || it.action == Action.LEFT_CLICK_AIR
                    block.handleInteract(it.player, isLeft)
                    break
                }
            }
        }

        packetEvent<PacketInteractEvent> {
            val entity = idToEntity[it.entityId] ?: return@packetEvent
            entity.handleInteract(it.player, it.isAttack)
        }
    }

    fun getChunkCacheBundle(chunkX: Int, chunkZ: Int, world: World): ChunkBundle? {
        val chunks = chunkCache[world.name] ?: return null
        val chunkId = ChunkId(chunkX, chunkZ)
        return chunks[chunkId]
    }

    fun getOrCreateChunkCacheBundle(chunkX: Int, chunkZ: Int, world: World): ChunkBundle {
        val chunks = chunkCache.getOrPut(world.name) { ConcurrentHashMap() }
        val chunkId = ChunkId(chunkX, chunkZ)
        val bundle = chunks.getOrPut(chunkId) { ChunkBundle() }
        return bundle
    }

    private fun handlePlayerRemove(player: Player) {
        for (tickableObject in tickableObjects) {
            handlePlayerRemove(player, tickableObject, true)
        }
    }

    internal fun handlePlayerRemove(player: Player, fakeObject: FakeObject, removeViewer: Boolean = false) {
        if (removeViewer) {
            fakeObject.removeViewer(player)
        } else {
            fakeObject.hide(player)
        }
    }

    class ChunkBundle {
        val blocks = mutableListOf<FakeBlock>()
        val entities = mutableListOf<FakeEntity>()
    }
}