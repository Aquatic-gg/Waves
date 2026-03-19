package gg.aquatic.clientside

import gg.aquatic.clientside.block.FakeBlock
import gg.aquatic.clientside.entity.FakeEntity
import gg.aquatic.clientside.meg.MEGInteractableHandler
import gg.aquatic.common.ChunkId
import gg.aquatic.common.event
import gg.aquatic.common.ticker.GlobalTicker
import gg.aquatic.kregistry.bootstrap.BootstrapHolder
import gg.aquatic.pakket.api.event.packet.PacketBlockChangeEvent
import gg.aquatic.pakket.api.event.packet.PacketChunkLoadEvent
import gg.aquatic.pakket.api.event.packet.PacketInteractEvent
import gg.aquatic.pakket.api.nms.BlockPos
import gg.aquatic.pakket.api.nms.toBlockPos
import gg.aquatic.pakket.isChunkTracked
import gg.aquatic.pakket.packetEvent
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
    //internal val locationToBlocks = ConcurrentHashMap<Location, MutableSet<FakeBlock>>()
    private val objectRemovalQueue: MutableSet<FakeObject> = ConcurrentHashMap.newKeySet()

    private val chunkCache = ConcurrentHashMap<String, MutableMap<ChunkId, ChunkBundle>>()

    private var tickCycle = 0
    internal lateinit var bootstrapHolder: BootstrapHolder

    fun initialize() {
        setupTicker()
        setupModelEngine()
        setupChunkEvents()
        setupPlayerEvents()
        setupInteractionEvents()
    }

    private fun setupTicker() {
        GlobalTicker.runRepeatFixedDelay(50L) {
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
        }
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
                updateVisibilityBatch(it.player, bundle.blocks.values.flatten())
                for (entity in bundle.entities) entity.updateVisibility(it.player)
            }
        }
        event<PlayerChunkUnloadEvent> {
            val bundle = getChunkCacheBundle(it.chunk.x, it.chunk.z, it.world) ?: return@event
            for (block in bundle.blocks.values.flatten()) block.updateVisibility(it.player)
            for (entity in bundle.entities) entity.updateVisibility(it.player)
        }
    }

    /**
     * Dynamically updates visibility for a batch of blocks in a single packet per chunk.
     * Combines hiding old blocks and showing new blocks into one multi-block change.
     */
    fun updateVisibilityBatch(player: Player, blocks: Collection<FakeBlock>) {
        val updatesByChunk = HashMap<org.bukkit.Chunk, MutableMap<Location, org.bukkit.block.data.BlockData>>()

        for (block in blocks) {
            val chunk = block.location.chunk
            if (!player.isChunkTracked(chunk)) continue

            val shouldSee = block.shouldSee(player)
            val isViewing = block.isPacketViewer(player)

            if (shouldSee && !isViewing) {
                val updateMap = updatesByChunk.getOrPut(chunk) { HashMap() }
                updateMap[block.location] = block.renderedBlockData()
                block.injectViewer(player)
            } else if (!shouldSee && isViewing) {
                val updateMap = updatesByChunk.getOrPut(chunk) { HashMap() }
                updateMap.putIfAbsent(block.location, block.location.block.blockData)
                block.ejectViewer(player)
            }
        }

        for (updateMap in updatesByChunk.values) {
            if (updateMap.isNotEmpty()) {
                player.sendMultiBlockChange(updateMap)
            }
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
        packetEvent<PacketBlockChangeEvent> { handlePacketBlockChange(it) }
        event<PlayerInteractEvent> { handlePlayerInteract(it) }
        packetEvent<PacketInteractEvent> { handleEntityInteract(it) }
    }

    private fun handlePacketBlockChange(event: PacketBlockChangeEvent) {
        val player = event.player
        val chunkX = Math.floorDiv(event.x, 16)
        val chunkZ = Math.floorDiv(event.z, 16)
        val bundle = getChunkCacheBundle(chunkX, chunkZ, player.world) ?: return
        val blockPos = Location(player.world, event.x.toDouble(), event.y.toDouble(), event.z.toDouble()).toBlockPos()
        val blocks = bundle.blocks[blockPos]
        if (blocks.isNullOrEmpty()) {
            return
        }

        for (block in blocks) {
            if (block.isAudienceMember(player) && !block.destroyed) {
                event.blockData = block.renderedBlockData()
                break
            }
        }
    }

    private fun handlePlayerInteract(event: PlayerInteractEvent) {
        if (event.hand == EquipmentSlot.OFF_HAND) return

        val clickedLocation = event.clickedBlock?.location ?: return

        val chunkX = clickedLocation.blockX shr 4
        val chunkZ = clickedLocation.blockZ shr 4

        val bundle = getChunkCacheBundle(chunkX, chunkZ, event.player.world) ?: return

        val blocks = bundle.blocks[clickedLocation.toBlockPos()] ?: return
        for (block in blocks) {
            if (block.destroyed) continue
            if (block.isAudienceMember(event.player)) {
                event.isCancelled = true
                val isLeft = event.action == Action.LEFT_CLICK_BLOCK || event.action == Action.LEFT_CLICK_AIR
                block.handleInteract(event.player, isLeft)
                break
            }
        }
    }

    private fun handleEntityInteract(event: PacketInteractEvent) {
        if (event.isSecondary) return
        if (event.interactType == PacketInteractEvent.InteractType.INTERACT_AT) return
        val entity = idToEntity[event.entityId] ?: return
        entity.handleInteract(event.player, event.isAttack)
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
        val blocks = ConcurrentHashMap<BlockPos, MutableCollection<FakeBlock>>()
        val entities = mutableListOf<FakeEntity>()
    }
}
