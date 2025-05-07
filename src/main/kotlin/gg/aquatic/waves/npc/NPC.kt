package gg.aquatic.waves.npc

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.PacketEntity
import gg.aquatic.waves.api.nms.profile.ProfileEntry
import gg.aquatic.waves.api.nms.profile.UserProfile
import gg.aquatic.waves.api.nms.scoreboard.Team
import gg.aquatic.waves.util.sendPacket
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class NPC(
    val profile: UserProfile,
    var gameMode: GameMode,
    var tabName: Component,
    var nameColor: NamedTextColor?,
    var prefix: Component?,
    var suffix: Component?,
    var teamName: String = UUID.randomUUID().toString(),
    var ping: Int,
    location: Location,
) {

    var viewers = Collections.synchronizedSet<UUID>(HashSet())
    var equipment = ConcurrentHashMap<EquipmentSlot, ItemStack?>()

    fun hasSpawned(player: Player): Boolean {
        return viewers.contains(player.uniqueId)
    }

    var location: Location = location
        private set(value) {
            field = value
            packetEntity = createEntity()
        }

    var packetEntity = createEntity()
        private set

    private fun createEntity(): PacketEntity {
        return Waves.NMS_HANDLER.createEntity(location, EntityType.PLAYER, profile.uuid)
            ?: throw Exception("Failed to create entity")
    }

    fun spawn(player: Player) {
        if (hasSpawned(player)) {
            return
        }
        show(player)

        viewers.add(player.uniqueId)
    }

    private fun show(player: Player) {
        val packet = Waves.NMS_HANDLER.createPlayerInfoUpdatePacket(
            1, ProfileEntry(
                profile, false, ping, gameMode, tabName, true, 0
            )
        )

        Waves.NMS_HANDLER.sendPacket(packet, false, player)
        player.sendPacket(packetEntity.spawnPacket, false)

        if (prefix != null || suffix != null || nameColor != null) {
            val teamPacket = Waves.NMS_HANDLER.createTeamsPacket(
                Team(
                    teamName,
                    prefix ?: Component.empty(),
                    suffix ?: Component.empty(),
                    org.bukkit.scoreboard.Team.OptionStatus.ALWAYS,
                    org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
                    nameColor ?: NamedTextColor.WHITE
                ),
                0,
                profile.name
            )
            player.sendPacket(teamPacket, false)
        }
    }

    fun despawn(player: Player) {
        if (!hasSpawned(player)) {
            return
        }
        hide(player)
        viewers.remove(player.uniqueId)
    }

    private fun hide(player: Player) {
        player.sendPacket(packetEntity.despawnpacket, false)
    }

    fun despawnAll() {
        for (uUID in viewers) {
            val player = Bukkit.getPlayer(uUID) ?: continue
            player.sendPacket(packetEntity.despawnpacket, false)
        }
        viewers.clear()
    }

    fun teleport(location: Location) {
        val hidePacket = packetEntity.despawnpacket
        this.location = location
        for (uUID in viewers) {
            val player = Bukkit.getPlayer(uUID) ?: continue
            player.sendPacket(hidePacket, false)
            show(player)
        }
    }

}