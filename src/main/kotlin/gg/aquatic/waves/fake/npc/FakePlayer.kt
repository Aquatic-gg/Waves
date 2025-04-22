package gg.aquatic.waves.fake.npc

import com.github.retrooper.packetevents.protocol.npc.NPC
import com.github.retrooper.packetevents.protocol.player.GameMode
import com.github.retrooper.packetevents.protocol.player.UserProfile
import gg.aquatic.waves.chunk.cache.ChunkCacheHandler
import gg.aquatic.waves.chunk.chunkId
import gg.aquatic.waves.fake.EntityBased
import gg.aquatic.waves.fake.FakeObject
import gg.aquatic.waves.fake.FakeObjectChunkBundle
import gg.aquatic.waves.fake.FakeObjectHandler
import gg.aquatic.waves.fake.entity.FakeEntityInteractEvent
import gg.aquatic.waves.util.audience.AquaticAudience
import gg.aquatic.waves.util.audience.FilterAudience
import gg.aquatic.waves.util.runAsync
import gg.aquatic.waves.util.runSync
import gg.aquatic.waves.util.toUser
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class FakePlayer(
    val profile: UserProfile,
    val tabName: Component?,
    val nameColor: NamedTextColor,
    val prefixName: Component?,
    val suffixName: Component?,
    val gameMode: GameMode = GameMode.CREATIVE,
    override var location: Location,
    override val viewRange: Int,
    audience: AquaticAudience,
    override var onInteract: (FakeEntityInteractEvent) -> Unit = {},
): FakeObject(), EntityBased {

    @Volatile
    override var audience: AquaticAudience = FilterAudience { false }
        set(value) {
            field = value
            for (viewer in viewers.toMutableList()) {
                if (field.canBeApplied(viewer) && viewer.isOnline) continue
                removeViewer(viewer)
            }
            for (player in
            location.world!!.players.filter { !viewers.contains(it) }) {
                if (!field.canBeApplied(player)) continue
                addViewer(player)
            }
        }


    val atomicEntityId = AtomicInteger(SpigotReflectionUtil.generateEntityId())
    override val entityId: Int get() = atomicEntityId.get()
    val entityUUID = profile.uuid
    val npc = NPC(profile, entityId, gameMode, tabName, nameColor, prefixName, suffixName)

    override fun destroy() {
        destroyed = true
        for (player in isViewing.toSet()) {
            hide(player)
        }
        FakeObjectHandler.tickableObjects -= this
        unregister()
        FakeObjectHandler.idToEntity -= entityId
    }

    init {
        this.audience = audience
        FakeObjectHandler.tickableObjects += this
        FakeObjectHandler.idToEntity += entityId to this

        runSync {
            val chunkViewers = location.chunk.playersSeeingChunk.toSet()
            runAsync {
                for (viewer in viewers) {
                    if (viewer in chunkViewers) {
                        show(viewer)
                    }
                }
            }
        }
    }

    fun register() {
        if (registered) return
        registered = true
        var bundle =
            ChunkCacheHandler.getObject(location.chunk, FakeObjectChunkBundle::class.java) as? FakeObjectChunkBundle
        if (bundle == null) {
            bundle = FakeObjectChunkBundle()
            ChunkCacheHandler.registerObject(bundle, location.chunk)
        }
        bundle.npcs += this
    }

    fun unregister() {
        if (!registered) return
        registered = false
        val bundle =
            ChunkCacheHandler.getObject(location.chunk, FakeObjectChunkBundle::class.java) as? FakeObjectChunkBundle
                ?: return
        bundle.npcs -= this
    }

    override fun addViewer(player: Player) {
        if (viewers.contains(player)) return
        viewers.add(player)
        if (player.world.name != location.world!!.name) return
        if (player.location.distanceSquared(location) <= viewRange * viewRange) {
            show(player)
        }
    }

    override fun removeViewer(uuid: UUID) {
        viewers.removeIf { it.uniqueId == uuid }
        isViewing.removeIf { it.uniqueId == uuid }
    }

    override fun removeViewer(player: Player) {
        if (isViewing.contains(player)) {
            hide(player)
        }
        FakeObjectHandler.handlePlayerRemove(player, this, true)
    }

    override fun show(player: Player) {
        if (isViewing.contains(player)) return
        val user = player.toUser() ?: return
        isViewing.add(player)

        npc.spawn(user.channel)
    }

    override fun hide(player: Player) {
        isViewing.remove(player)
        val user = player.toUser() ?: return
        npc.despawn(user.channel)
    }

    override fun tick() {

    }

    fun teleport(location: Location) {
        npc.teleport(SpigotConversionUtil.fromBukkitLocation(location))
        this.location = location
        if (registered) {
            unregister()
            register()
        }
    }
}