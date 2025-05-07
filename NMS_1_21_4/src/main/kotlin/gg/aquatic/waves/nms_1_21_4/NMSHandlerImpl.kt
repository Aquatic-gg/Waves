package gg.aquatic.waves.nms_1_21_4

import com.google.common.collect.LinkedHashMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import com.mojang.datafixers.util.Pair
import gg.aquatic.waves.api.ReflectionUtils
import gg.aquatic.waves.api.nms.NMSHandler
import gg.aquatic.waves.api.nms.PacketEntity
import gg.aquatic.waves.api.nms.ProtectedPacket
import gg.aquatic.waves.api.nms.profile.ProfileEntry
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.kyori.adventure.text.Component
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import net.minecraft.server.network.ServerPlayerConnection
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.entity.Relative
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_21_R3.CraftServer
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftMenuType
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.scoreboard.Team
import org.bukkit.util.Vector
import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.jvm.optionals.getOrNull

object NMSHandlerImpl : NMSHandler {

    private val playerConnectionField =
        ReflectionUtils.getField("connection", ServerCommonPacketListenerImpl::class.java)
    private val entityCounterField = ReflectionUtils.getStatic<AtomicInteger>("ENTITY_COUNTER", Entity::class.java)
    private val setPassengersConstructor =
        ClientboundSetPassengersPacket::class.java.getConstructor(FriendlyByteBuf::class.java).apply {
            isAccessible = true
        }
    private val listeners = ConcurrentHashMap<UUID, PacketListener>()

    override fun injectPacketListener(player: Player) {
        val craftPlayer = (player as CraftPlayer)
        val packetListener = PacketListener(craftPlayer)
        val connection = playerConnectionField.get(craftPlayer.handle.connection) as Connection
        val pipeline = connection.channel.pipeline()

        for ((_, handler) in pipeline.toMap()) {
            if (handler is Connection) {
                pipeline.addBefore("packet_handler", "waves_packet_listener", packetListener)
                break
            }
        }
    }

    override fun unregisterPacketListener(player: Player) {
        val craftPlayer = (player as CraftPlayer)
        val connection = playerConnectionField.get(craftPlayer.handle.connection) as Connection
        val channel = connection.channel
        val pipeline = channel.pipeline()
        if (channel != null) {
            try {
                if (pipeline.names().contains("waves_packet_listener")) {
                    pipeline.remove("waves_packet_listener")
                }
            } catch (_: Exception) {
            }
        }
    }


    override fun chunkViewers(chunk: Chunk): Collection<Player> {
        val craftWorld = chunk.world as CraftWorld
        return craftWorld.handle.chunkSource.chunkMap.getPlayers(ChunkPos(chunk.x, chunk.z), false)
            .map { it.bukkitEntity as Player }
    }

    override fun trackedChunks(player: Player): Collection<Chunk> {
        val chunkPositions = HashSet<ChunkPos>()
        (player as CraftPlayer).handle.chunkTrackingView.forEach { chunkPos ->
            chunkPositions.add(chunkPos)
        }
        val craftWorld = player.world as CraftWorld
        return chunkPositions.mapNotNull {
            val chunk = player.world.getChunkAt(it.x, it.z)
            val players =
                craftWorld.handle.chunkSource.chunkMap.getPlayers(ChunkPos(chunk.x, chunk.z), false).map { p -> p.uuid }
            if (players.contains(player.uniqueId)) {
                chunk
            } else {
                null
            }
        }
    }

    override fun showEntity(location: Location, entityType: EntityType, vararg player: Player): PacketEntity? {
        val packetEntity = createEntity(location, entityType, null) ?: return null

        for (item in player) {
            item.sendPacket(packetEntity.spawnPacket as Packet<*>)
        }
        return packetEntity
    }

    override fun createEntity(location: Location, entityType: EntityType, uuid: UUID?): PacketEntity? {
        val nmsEntityType =
            net.minecraft.world.entity.EntityType.byString(entityType.name.lowercase())?.getOrNull() ?: return null
        //val id = generateEntityId()

        val worldServer = (location.world as CraftWorld).handle
        val entity =
            createEntity(nmsEntityType, uuid, worldServer, BlockPos(location.blockX, location.blockY, location.blockZ))
                ?: return null

        entity.absMoveTo(location.x, location.y, location.z, location.yaw, location.pitch)

        val seenBy = HashSet<ServerPlayerConnection>()
        val tracker = ServerEntity(
            worldServer,
            entity,
            entity.type.updateInterval(),
            true,
            { },
            seenBy,
        )
        return PacketEntity(
            entity.id,
            entity,
            entity.getAddEntityPacket(tracker),
            despawnpacket = ClientboundRemoveEntitiesPacket(entity.id)
        )
    }

    private fun <T : Entity> createEntity(
        entityType: net.minecraft.world.entity.EntityType<T>,
        uuid: UUID?,
        worldServer: ServerLevel,
        blockPos: BlockPos
    ): T? {
        val entity = entityType.create(worldServer, EntitySpawnReason.COMMAND)
        entity?.let {
            it.moveTo(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), 0.0f, 0.0f)
            worldServer.addFreshEntityWithPassengers(it)

            if (uuid != null) {
                it.uuid = uuid
            }

            if (it is Mob) {
                it.yHeadRot = it.yRot
                it.yBodyRot = it.yRot
            }
        }
        return entity
    }

    override fun updateEntity(
        packetEntity: PacketEntity,
        consumer: (org.bukkit.entity.Entity) -> Unit,
        vararg players: Player
    ) {
        val packet = createEntityUpdatePacket(packetEntity, consumer) as Packet<*>
        packetEntity.updatePacket = packet

        for (player in players) {
            player.sendPacket(packet)
        }
    }

    override fun createTeleportPacket(entityId: Int, location: Location, previousLocation: Vector): Any {
        val delta = previousLocation.clone().subtract(location.toVector())
        val packet = ClientboundTeleportEntityPacket(
            entityId, PositionMoveRotation(
                Vec3(location.x, location.y, location.z),
                Vec3(delta.x, delta.y, delta.z),
                location.yaw,
                location.pitch
            ), Relative.ALL, false
        )
        return packet
    }

    override fun createEntityUpdatePacket(
        packetEntity: PacketEntity,
        consumer: (org.bukkit.entity.Entity) -> Unit
    ): Any {
        val entity = (packetEntity.entityInstance as Entity).bukkitEntity.apply {
            consumer(this)
        }.handle

        val packet = ClientboundSetEntityDataPacket(
            entity.id,
            entity.entityData.nonDefaultValues
                ?: emptyList<net.minecraft.network.syncher.SynchedEntityData.DataValue<*>>()
        )
        return packet
    }

    override fun setPassengers(packetEntity: PacketEntity, passengerIds: IntArray, vararg players: Player) {
        val packet = createPassengersPacket(packetEntity, passengerIds) as Packet<*>
        packetEntity.passengerPacket = packet
        for (player in players) {
            player.sendPacket(packet)
        }
    }

    override fun createPassengersPacket(packetEntity: PacketEntity, passengerIds: IntArray): Any {
        val bytebuf = FriendlyByteBuf(Unpooled.buffer())
        bytebuf.writeVarInt(packetEntity.entityId)
        bytebuf.writeVarIntArray(passengerIds)

        val packet = setPassengersConstructor.newInstance(bytebuf)
        return packet
    }

    override fun setEquipment(
        packetEntity: PacketEntity,
        equipment: Map<EquipmentSlot, ItemStack?>,
        vararg players: Player
    ) {
        val packet = createEquipmentPacket(packetEntity, equipment) as Packet<*>
        packetEntity.equipmentPacket = packet

        for (player in players) {
            player.sendPacket(packet)
        }
    }

    override fun createEquipmentPacket(packetEntity: PacketEntity, equipment: Map<EquipmentSlot, ItemStack?>): Any {
        val mappedEquipment = equipment.map {
            Pair(net.minecraft.world.entity.EquipmentSlot.entries[it.key.ordinal], (it.value?.let { item ->
                CraftItemStack.asNMSCopy(
                    item
                )
            } ?: net.minecraft.world.item.ItemStack.EMPTY))
        }
        val packet = ClientboundSetEquipmentPacket(packetEntity.entityId, mappedEquipment)
        return packet
    }

    private val propertiesMapField = ReflectionUtils.getField("properties", PropertyMap::class.java).apply {
        this.isAccessible = true
    }

    private val entriesField =
        ReflectionUtils.getField("entries", ClientboundPlayerInfoUpdatePacket::class.java).apply {
            this.isAccessible = true
        }

    override fun createPlayerInfoUpdatePacket(actionId: Int, profileEntry: ProfileEntry): Any {
        return createPlayerInfoUpdatePacket(listOf(actionId), listOf(profileEntry))
    }

    override fun createPlayerInfoUpdatePacket(
        actionIds: Collection<Int>,
        profileEntries: Collection<ProfileEntry>
    ): Any {
        val entries = ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>()

        entries += profileEntries.map { profileEntry ->
            ClientboundPlayerInfoUpdatePacket.Entry(
                profileEntry.userProfile.uuid,
                GameProfile(profileEntry.userProfile.uuid, profileEntry.userProfile.name).apply {
                    val multiMap = LinkedHashMultimap.create<String, Property>()
                    for (property in profileEntry.userProfile.textureProperties) {
                        multiMap.put("textures", Property(property.name, property.value, property.signature))
                    }
                    propertiesMapField.set(properties, multiMap)
                },
                profileEntry.listed,
                profileEntry.latency,
                GameType.entries[profileEntry.gameMode.ordinal],
                profileEntry.displayName?.toNMSComponent(),
                profileEntry.showHat,
                profileEntry.listOrder,
                null
            )
        }

        val packet = ClientboundPlayerInfoUpdatePacket(
            EnumSet.copyOf(actionIds.map { ClientboundPlayerInfoUpdatePacket.Action.entries[it] }.toMutableList()),
            mutableListOf<ServerPlayer>()
        )
        entriesField.set(packet, entries)
        return packet
    }

    override fun createTeamsPacket(team: gg.aquatic.waves.api.nms.scoreboard.Team, actionId: Int, playerName: String): Any {
        val scoreboard = Scoreboard()
        val playerTeam = PlayerTeam(
            scoreboard,
            team.teamName
        )
        playerTeam.playerPrefix = team.prefix.toNMSComponent()
        playerTeam.playerSuffix = team.suffix.toNMSComponent()
        playerTeam.collisionRule = net.minecraft.world.scores.Team.CollisionRule.entries[team.collisionRule.ordinal]
        playerTeam.nameTagVisibility =
            net.minecraft.world.scores.Team.Visibility.entries[team.nametagVisibility.ordinal]
        playerTeam.color = ChatFormatting.valueOf(team.nameColor.toString())
        val packet = ClientboundSetPlayerTeamPacket.createPlayerPacket(
            playerTeam, playerName,
            ClientboundSetPlayerTeamPacket.Action.entries[actionId]
        )
        return packet
    }

    override fun setSlotItem(
        inventoryId: Int,
        stateId: Int,
        slot: Int,
        itemStack: ItemStack?,
        vararg players: Player
    ) {
        val packet = ClientboundContainerSetSlotPacket(
            inventoryId,
            stateId,
            slot,
            itemStack?.toNMS() ?: net.minecraft.world.item.ItemStack.EMPTY
        )

        for (player in players) {
            player.sendPacket(packet)
        }
    }

    override fun setWindowItems(
        inventoryId: Int,
        stateId: Int,
        items: Collection<ItemStack?>,
        carriedItem: ItemStack?,
        vararg players: Player
    ) {
        val nmsItems = NonNullList.create<net.minecraft.world.item.ItemStack>()
        nmsItems += items.mapNotNull { it?.toNMS() }
        val packet = ClientboundContainerSetContentPacket(
            inventoryId,
            stateId,
            nmsItems,
            carriedItem?.toNMS() ?: net.minecraft.world.item.ItemStack.EMPTY
        )
        for (player in players) {
            player.sendPacket(packet)
        }
    }

    override fun openWindowPacket(
        inventoryId: Int,
        menuType: MenuType,
        title: Component,
    ): Any {
        val packet = ClientboundOpenScreenPacket(
            inventoryId,
            CraftMenuType.bukkitToMinecraft(menuType),
            title.toNMSComponent()
        )
        return packet
    }

    override fun openWindow(
        inventoryId: Int,
        menuType: MenuType,
        title: Component,
        vararg players: Player
    ) {
        val packet = openWindowPacket(inventoryId, menuType, title) as Packet<*>

        for (player in players) {
            player.sendPacket(packet)
        }
    }

    override fun receiveWindowClick(
        inventoryId: Int,
        stateId: Int,
        slot: Int,
        buttonNum: Int,
        clickTypeNum: Int,
        carriedItem: ItemStack?,
        changedSlots: Map<Int, ItemStack?>,
        vararg players: Player
    ) {
        val map = Int2ObjectOpenHashMap<net.minecraft.world.item.ItemStack>()
        changedSlots.forEach { (key, value) ->
            map[key] = value?.toNMS() ?: net.minecraft.world.item.ItemStack.EMPTY
        }

        val packet = ServerboundContainerClickPacket(
            inventoryId,
            stateId,
            slot,
            buttonNum,
            ClickType.entries[clickTypeNum],
            carriedItem?.toNMS() ?: net.minecraft.world.item.ItemStack.EMPTY,
            map
        )

        for (player in players) {
            (player as CraftPlayer).handle.connection.handleContainerClick(packet)
        }
    }

    override fun generateEntityId(): Int {
        return entityCounterField.getAndIncrement()
    }

    private fun ItemStack.toNMS(): net.minecraft.world.item.ItemStack {
        return CraftItemStack.asNMSCopy(this)
    }

    private fun Player.sendPacket(packet: Packet<*>) {
        (this as CraftPlayer).handle.connection.send(packet)
    }


    override fun sendPacket(packet: Any, silent: Boolean, vararg players: Player) {
        if (packet !is Packet<*>) return
        for (player in players) {
            if (silent) {
                val protectedPacket = ProtectedPacket(packet)
                val playerConnection =
                    playerConnectionField.get((player as CraftPlayer).handle.connection) as Connection
                playerConnection.channel.pipeline().write(protectedPacket)
            } else {
                player.sendPacket(packet)
            }
        }
    }

    fun Component.toNMSComponent(): net.minecraft.network.chat.Component {
        val kyoriJson = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(this)
        return net.minecraft.network.chat.Component.Serializer.fromJson(
            kyoriJson,
            ((Bukkit.getServer() as CraftServer).worlds.first() as CraftWorld).handle.registryAccess()
        )!!
    }
}