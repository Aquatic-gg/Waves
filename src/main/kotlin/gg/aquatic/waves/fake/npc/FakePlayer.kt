package gg.aquatic.waves.fake.npc

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.profile.UserProfile
import gg.aquatic.waves.chunk.cache.ChunkCacheHandler
import gg.aquatic.waves.chunk.trackedBy
import gg.aquatic.waves.fake.EntityBased
import gg.aquatic.waves.fake.FakeObject
import gg.aquatic.waves.fake.FakeObjectChunkBundle
import gg.aquatic.waves.fake.FakeObjectHandler
import gg.aquatic.waves.fake.entity.FakeEntityInteractEvent
import gg.aquatic.waves.npc.NPC
import gg.aquatic.waves.util.*
import gg.aquatic.waves.util.audience.AquaticAudience
import gg.aquatic.waves.util.audience.FilterAudience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

class FakePlayer(
    val profile: UserProfile,
    tabName: Component,
    nameColor: NamedTextColor,
    prefixName: Component?,
    suffixName: Component?,
    gameMode: GameMode = GameMode.CREATIVE,
    location: Location,
    override val viewRange: Int,
    audience: AquaticAudience,
    override var onInteract: (FakeEntityInteractEvent) -> Unit = {},
) : FakeObject(), EntityBased {

    val npc =
        NPC(profile, gameMode, tabName, nameColor, prefixName, suffixName, "npc-${UUID.randomUUID()}", 1, location)
    override var location: Location = location
        get() = npc.location
        private set(value) {
            field = value
            teleport(value)
        }

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


    override val entityId: Int get() = npc.packetEntity.entityId

    override fun destroy() {
        destroyed = true
        for (player in isViewing.toSet()) {
            hide(player)
        }
        FakeObjectHandler.tickableObjects -= this
        unregister()
        FakeObjectHandler.idToEntity -= entityId
    }


    fun updateEntity(func: NPC.() -> Unit) {
        val hadPassengers = npc.passengers.isNotEmpty()
        func(npc)

        npc.packetEntity.setData(npc.entityData.values)

        if (npc.passengers.isNotEmpty()) {
            npc.packetEntity.setPassengers(npc.passengers.toIntArray())
        }

        npc.packetEntity.setEquipment(npc.equipment)

        val players = isViewing.toTypedArray()
        npc.packetEntity.sendDataUpdate(Waves.NMS_HANDLER, false,*players)
        if (!(!hadPassengers && npc.passengers.isEmpty())) {
            npc.packetEntity.sendPassengerUpdate(Waves.NMS_HANDLER, false,*players)
        }
        npc.packetEntity.sendEquipmentUpdate(Waves.NMS_HANDLER,*players)
    }

    init {
        this.audience = audience
        FakeObjectHandler.tickableObjects += this
        FakeObjectHandler.idToEntity += entityId to this

        runSync {
            val chunkViewers = location.chunk.trackedBy().toSet()
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
        isViewing.add(player)
        npc.spawn(player)
    }

    override fun hide(player: Player) {
        isViewing.remove(player)
        npc.despawn(player)
    }

    override fun tick() {

    }

    fun teleport(location: Location) {
        npc.teleport(location)
        if (registered) {
            unregister()
            register()
        }
    }
}