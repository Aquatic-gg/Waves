package gg.aquatic.waves.nms_1_21_7

import com.google.common.collect.LinkedHashMultimap
import com.google.common.hash.HashCode
import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import gg.aquatic.waves.api.ReflectionUtils
import gg.aquatic.waves.api.nms.*
import gg.aquatic.waves.api.nms.entity.DataSerializerTypes
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.api.nms.profile.GameEventAction
import gg.aquatic.waves.api.nms.profile.ProfileEntry
import gg.aquatic.waves.api.nms.scoreboard.Team
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.ChatFormatting
import net.minecraft.core.NonNullList
import net.minecraft.core.Registry
import net.minecraft.core.Rotations
import net.minecraft.core.component.TypedDataComponent
import net.minecraft.core.registries.Registries
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.HashedPatchMap
import net.minecraft.network.HashedStack
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.RegistryOps
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import net.minecraft.server.network.ServerPlayerConnection
import net.minecraft.util.HashOps
import net.minecraft.world.entity.*
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.GameType
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.LevelChunkSection
import net.minecraft.world.level.chunk.PalettedContainer
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.craftbukkit.inventory.CraftMenuType
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.jvm.optionals.getOrNull
import kotlin.math.absoluteValue

object NMSHandlerImpl : NMSHandler {

    private val playerConnectionField =
        ReflectionUtils.getField("connection", ServerCommonPacketListenerImpl::class.java)
    private val entityCounterField = ReflectionUtils.getStatic<AtomicInteger>("ENTITY_COUNTER", Entity::class.java)
    private val setPassengersConstructor =
        ClientboundSetPassengersPacket::class.java.getDeclaredConstructor(FriendlyByteBuf::class.java).apply {
            isAccessible = true
        }

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

    override fun createBundlePacket(packets: Collection<Any>): Any {
        val packet = ClientboundBundlePacket(
            packets.map { it as Packet<ClientGamePacketListener> }
        )
        return packet
    }

    override fun showEntity(location: Location, entityType: EntityType, vararg player: Player): PacketEntity? {
        val packetEntity = createEntity(location, entityType, null) ?: return null

        for (item in player) {
            item.sendPacket(packetEntity.spawnPacket as Packet<*>)
        }
        return packetEntity
    }

    private fun Entity.absMoveTo(x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
        this.setPos(x, y, z)
        this.yRot = yaw
        this.xRot = pitch
        this.setYBodyRot(yaw)
    }

    override fun createEntity(location: Location, entityType: EntityType, uuid: UUID?): PacketEntity? {
        val nmsEntityType =
            net.minecraft.world.entity.EntityType.byString(entityType.name.lowercase()).getOrNull() ?: return null
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
            { _, _ -> },
            seenBy,
        )
        return PacketEntity(
            location,
            entity.id,
            entity,
            entity.getAddEntityPacket(tracker),
            despawnpacket = ClientboundRemoveEntitiesPacket(entity.id)
        )
    }

    override fun recreateEntityPacket(
        packetEntity: PacketEntity,
        location: Location,
    ): Any {
        val entity = packetEntity.entityInstance as Entity
        entity.absMoveTo(location.x, location.y, location.z, location.yaw, location.pitch)
        return entity.getAddEntityPacket(
            ServerEntity(
                (location.world as CraftWorld).handle,
                entity,
                entity.type.updateInterval(),
                true,
                { },
                { _, _ -> },
                HashSet()
            )
        )
    }

    private fun <T : Entity> createEntity(
        entityType: net.minecraft.world.entity.EntityType<T>,
        uuid: UUID?,
        worldServer: ServerLevel,
        blockPos: BlockPos,
    ): T? {
        val entity = entityType.create(worldServer, EntitySpawnReason.COMMAND)
        entity?.let {
            it.absMoveTo(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), 0.0f, 0.0f)
            //worldServer.addFreshEntityWithPassengers(it)

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
        vararg players: Player,
    ) {
        val packet = createEntityUpdatePacket(packetEntity, consumer) as Packet<*>
        packetEntity.updatePacket = packet

        for (player in players) {
            player.sendPacket(packet)
        }
    }

    override fun createTeleportPacket(entityId: Int, location: Location, previousLocation: org.bukkit.util.Vector): Any {
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

    private fun mapEntityDataValue(original: EntityDataValue): SynchedEntityData.DataValue<*>? {
        when(original.serializerType) {
            DataSerializerTypes.BYTE -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.BYTE,
                    original.value as Byte
                )
            }
            DataSerializerTypes.INT -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.INT,
                    original.value as Int
                )
            }
            DataSerializerTypes.FLOAT -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.FLOAT,
                    original.value as Float
                )
            }
            DataSerializerTypes.STRING -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.STRING,
                    original.value as String
                )
            }

            DataSerializerTypes.BOOLEAN -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.BOOLEAN,
                    original.value as Boolean
                )
            }
            DataSerializerTypes.OPTIONAL_COMPONENT -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.OPTIONAL_COMPONENT,
                    (original.value as Optional<Component>).getOrNull().let {
                        val nmsComponent = it?.toNMSComponent()
                        Optional.ofNullable(nmsComponent)
                    }
                )
            }
            DataSerializerTypes.ITEM_STACK -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.ITEM_STACK,
                    CraftItemStack.asNMSCopy(original.value as ItemStack)
                )
            }
            DataSerializerTypes.ROTATIONS -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.ROTATIONS,
                    (original.value as org.bukkit.util.Vector).let {
                        Rotations(it.x.toFloat(), it.y.toFloat(), it.z.toFloat())
                    }
                )
            }
            DataSerializerTypes.BLOCK_POS -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.BLOCK_POS,
                    (original.value as BlockPos).let {
                        net.minecraft.core.BlockPos(it.x, it.y, it.z)
                    }
                )
            }

            DataSerializerTypes.BLOCK_STATE -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.BLOCK_STATE,
                    (original.value as CraftBlockData).state
                )
            }

            DataSerializerTypes.COMPONENT -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.COMPONENT,
                    (original.value as Component).toNMSComponent()
                )
            }
            DataSerializerTypes.LONG -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.LONG,
                    original.value as Long
                )
            }

            DataSerializerTypes.POSE -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.POSE,
                    (original.value as Pose).let {
                        net.minecraft.world.entity.Pose.entries[it.ordinal]
                    }
                )
            }
            DataSerializerTypes.VECTOR3 -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.VECTOR3,
                    (original.value as Vector3f)
                )
            }
            DataSerializerTypes.DIRECTION -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.DIRECTION,
                    (original.value as Direction).let {
                        net.minecraft.core.Direction.entries[it.ordinal]
                    }
                )
            }
            DataSerializerTypes.OPTIONAL_BLOCK_POS -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.OPTIONAL_BLOCK_POS,
                    (original.value as Optional<BlockPos>).let {
                        Optional.ofNullable(it.getOrNull()?.let { pos -> net.minecraft.core.BlockPos(pos.x, pos.y, pos.z) })
                    }
                )
            }
            DataSerializerTypes.OPTIONAL_BLOCK_STATE -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.OPTIONAL_BLOCK_STATE,
                    (original.value as Optional<BlockData>).let {
                        Optional.ofNullable(it.getOrNull()?.let { blockData -> (blockData as CraftBlockData).state })
                    }
                )
            }

            DataSerializerTypes.QUATERNION -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.QUATERNION,
                    original.value as Quaternionf
                )
            }
            DataSerializerTypes.OPTIONAL_UNSIGNED_INT -> {
                return SynchedEntityData.DataValue(
                    original.id,
                    EntityDataSerializers.OPTIONAL_UNSIGNED_INT,
                    (original.value as Optional<Int>).getOrNull().let {
                        if (it == null) {
                            OptionalInt.empty()
                        } else OptionalInt.of(it)
                    }
                )
            }
        }
        return null
    }

    override fun createEntityUpdatePacket(id: Int, values: Collection<EntityDataValue>): Any {
        val data = values.mapNotNull { mapEntityDataValue(it) }
        val packet = ClientboundSetEntityDataPacket(id,data)
        return packet
    }

    override fun createEntityUpdatePacket(
        packetEntity: PacketEntity,
        consumer: (org.bukkit.entity.Entity) -> Unit,
    ): Any {
        val entity = (packetEntity.entityInstance as Entity).bukkitEntity.apply {
            consumer(this)
        }.handle

        val packet = ClientboundSetEntityDataPacket(
            entity.id,
            entity.entityData.nonDefaultValues
                ?: emptyList<SynchedEntityData.DataValue<*>>()
        )
        return packet
    }

    override fun setPassengers(packetEntity: PacketEntity, passengerIds: IntArray, vararg players: Player) {
        val packet = createPassengersPacket(packetEntity.entityId, passengerIds) as Packet<*>
        packetEntity.passengerPacket = packet
        for (player in players) {
            player.sendPacket(packet)
        }
    }

    override fun createPassengersPacket(holderId: Int, passengerIds: IntArray): Any {
        val bytebuf = FriendlyByteBuf(Unpooled.buffer())
        bytebuf.writeVarInt(holderId)
        bytebuf.writeVarIntArray(passengerIds)

        val packet = setPassengersConstructor.newInstance(bytebuf)
        return packet
    }

    override fun setEquipment(
        packetEntity: PacketEntity,
        equipment: Map<EquipmentSlot, ItemStack?>,
        vararg players: Player,
    ) {
        val packet = createEquipmentPacket(packetEntity, equipment) as Packet<*>
        //packetEntity.equipmentPacket = packet

        for (player in players) {
            player.sendPacket(packet)
        }
    }

    override fun createEquipmentPacket(packetEntity: PacketEntity, equipment: Map<EquipmentSlot, ItemStack?>): Any {
        val mappedEquipment = equipment.map {
            com.mojang.datafixers.util.Pair(net.minecraft.world.entity.EquipmentSlot.entries[it.key.ordinal], (it.value?.let { item ->
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
        profileEntries: Collection<ProfileEntry>,
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

    private val gameStateTypesMapper = hashMapOf(
        GameEventAction.NO_RESPAWN_BLOCK_AVAILABLE to ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE,
        GameEventAction.START_RAINING to ClientboundGameEventPacket.START_RAINING,
        GameEventAction.STOP_RAINING to ClientboundGameEventPacket.STOP_RAINING,
        GameEventAction.CHANGE_GAME_MODE to ClientboundGameEventPacket.CHANGE_GAME_MODE,
        GameEventAction.WIN_GAME to ClientboundGameEventPacket.WIN_GAME,
        GameEventAction.DEMO_EVENT to ClientboundGameEventPacket.DEMO_EVENT,
        GameEventAction.ARROW_HIT_PLAYER to ClientboundGameEventPacket.PLAY_ARROW_HIT_SOUND,
        GameEventAction.RAIN_LEVEL_CHANGE to ClientboundGameEventPacket.RAIN_LEVEL_CHANGE,
        GameEventAction.THUNDER_LEVEL_CHANGE to ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE,
        GameEventAction.PUFFER_FISH_STING to ClientboundGameEventPacket.PUFFER_FISH_STING,
        GameEventAction.GUARDIAN_ELDER_EFFECT to ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT,
        GameEventAction.IMMEDIATE_RESPAWN to ClientboundGameEventPacket.IMMEDIATE_RESPAWN,
        GameEventAction.LIMITED_CRAFTING to ClientboundGameEventPacket.LIMITED_CRAFTING,
        GameEventAction.LEVEL_CHUNKS_LOAD_START to ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START
    )

    override fun createChangeGameStatePacket(action: GameEventAction, value: Float): Any {
        val mappedAction =
            gameStateTypesMapper[action] ?: throw IllegalArgumentException("Unknown game event action: $action")
        val packet = ClientboundGameEventPacket(mappedAction, value)
        return packet
    }

    private val cameraPacketConstructor =
        ClientboundSetCameraPacket::class.java.getDeclaredConstructor(FriendlyByteBuf::class.java).apply {
            isAccessible = true
        }

    override fun createCameraPacket(entityId: Int): Any {
        val bytebuf = FriendlyByteBuf(Unpooled.buffer())
        bytebuf.writeVarInt(entityId)
        return cameraPacketConstructor.newInstance(bytebuf)
    }

    override fun createBlockChangePacket(location: Location, blockState: BlockData): Any {
        val packet = ClientboundBlockUpdatePacket(
            net.minecraft.core.BlockPos(location.blockX, location.blockY, location.blockZ),
            (blockState as CraftBlockData).state
        )
        return packet
    }

    override fun createEntityMotionPacket(entityId: Int, motion: Vector): Any {
        val packet = ClientboundSetEntityMotionPacket(entityId, Vec3(motion.x, motion.y, motion.z))
        return packet
    }

    override fun getBukkitEntity(packetEntity: PacketEntity): org.bukkit.entity.Entity {
        val entity = packetEntity.entityInstance as Entity
        return entity.bukkitEntity
    }

    private val chunkDataBufferField =
        ReflectionUtils.getField("buffer", ClientboundLevelChunkPacketData::class.java).apply {
            isAccessible = true
        }

    override fun modifyChunkPacketBlocks(world: World, packet: Any, func: (List<WrappedChunkSection>) -> Unit) {
        val sections = (world.minHeight.absoluteValue + world.maxHeight) shr 4
        val chunkBundlePacket = packet as ClientboundLevelChunkWithLightPacket
        val chunkData = chunkBundlePacket.chunkData
        val readBuffer = chunkData.readBuffer

        val wrappedSections = mutableListOf<Pair<WrappedChunkSection, LevelChunkSection>>()
        val registries = (world as CraftWorld).handle.registryAccess()
        val registry: Registry<Biome?> = registries.lookupOrThrow<Biome?>(Registries.BIOME)
        for (i in 0 until sections) {

            val container1 = PalettedContainer(
                Block.BLOCK_STATE_REGISTRY,
                Blocks.AIR.defaultBlockState(),
                PalettedContainer.Strategy.SECTION_STATES,
                null
            )
            val container2 = PalettedContainer(
                registry.asHolderIdMap(),
                registry.getOrThrow(Biomes.PLAINS),
                PalettedContainer.Strategy.SECTION_BIOMES,
                null // Paper - Anti-Xray - Add preset biomes
            )
            val section = LevelChunkSection(container1, container2)
            section.read(readBuffer)
            val pair = ((object : WrappedChunkSection {
                override fun set(x: Int, y: Int, z: Int, blockState: BlockData) {
                    section.setBlockState(x, y, z, (blockState as CraftBlockData).state, false)
                    //palettedContainer.set(x, y, z, (blockState as CraftBlockState).handle)
                }

                override fun get(x: Int, y: Int, z: Int): BlockData {
                    val state = CraftBlockData.fromData(section.getBlockState(x, y, z))
                    return state
                }
            } as WrappedChunkSection) to section)
            wrappedSections.add(pair)
        }
        func(wrappedSections.map { it.first })

        val bytes = ByteArray(calculateChunkSize(wrappedSections.map { it.second }))
        val writeBuffer: ByteBuf = Unpooled.wrappedBuffer(bytes)
        writeBuffer.writerIndex(0)

        extractChunkData(wrappedSections.map { it.second }, writeBuffer)
        chunkDataBufferField.set(chunkData, bytes)
    }

    private fun calculateChunkSize(sections: Collection<LevelChunkSection>): Int {
        var i = 0

        for (levelChunkSection in sections) {
            i += levelChunkSection.serializedSize
        }

        return i
    }

    private fun extractChunkData(sections: Collection<LevelChunkSection>, wrapper: ByteBuf) {
        var chunkSectionIndex = 0

        val buffer = FriendlyByteBuf(wrapper)

        for (levelChunkSection in sections) {
            levelChunkSection.write(buffer, null, chunkSectionIndex)
            ++chunkSectionIndex
        }
    }

    override fun createTeamsPacket(
        team: Team,
        actionId: Int,
        playerName: String,
    ): Any {
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

    override fun createSetSlotItemPacket(inventoryId: Int, stateId: Int, slot: Int, itemStack: ItemStack?): Any {
        val packet = ClientboundContainerSetSlotPacket(
            inventoryId,
            stateId,
            slot,
            itemStack?.toNMS() ?: net.minecraft.world.item.ItemStack.EMPTY
        )
        return packet
    }

    override fun setSlotItem(
        inventoryId: Int,
        stateId: Int,
        slot: Int,
        itemStack: ItemStack?,
        vararg players: Player,
    ) {
        val packet = createSetSlotItemPacket(inventoryId, stateId, slot, itemStack) as Packet<*>
        for (player in players) {
            player.sendPacket(packet)
        }
    }

    override fun createSetWindowItemsPacket(
        inventoryId: Int,
        stateId: Int,
        items: Collection<ItemStack?>,
        carriedItem: ItemStack?,
    ): Any {
        val nmsItems = NonNullList.create<net.minecraft.world.item.ItemStack>()
        nmsItems += items.map { it?.toNMS() ?: net.minecraft.world.item.ItemStack.EMPTY }
        val packet = ClientboundContainerSetContentPacket(
            inventoryId,
            stateId,
            nmsItems,
            carriedItem?.toNMS() ?: net.minecraft.world.item.ItemStack.EMPTY
        )
        return packet
    }

    override fun setWindowItems(
        inventoryId: Int,
        stateId: Int,
        items: Collection<ItemStack?>,
        carriedItem: ItemStack?,
        vararg players: Player,
    ) {
        val packet = createSetWindowItemsPacket(inventoryId, stateId, items, carriedItem) as Packet<*>
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
        vararg players: Player,
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
        vararg players: Player,
    ) {
        val registryAccess = (Bukkit.getWorlds().first() as CraftWorld).handle.registryAccess()
        val registryOps: RegistryOps<HashCode> = registryAccess.createSerializationContext(HashOps.CRC32C_INSTANCE);
        val hashOpsGenerator: HashedPatchMap.HashGenerator = object : HashedPatchMap.HashGenerator {
            override fun apply(typedDataComponent: TypedDataComponent<*>): Int {
                return typedDataComponent.encodeValue(registryOps).getOrThrow { string ->
                    IllegalArgumentException("Failed to hash $typedDataComponent: $string")
                }.asInt()
            }
        }

        val map = Int2ObjectOpenHashMap<HashedStack>()
        changedSlots.forEach { (key, value) ->
            val nmsItem = value?.toNMS() ?: net.minecraft.world.item.ItemStack.EMPTY
            map[key] = HashedStack.create(nmsItem, hashOpsGenerator)
        }

        val packet = ServerboundContainerClickPacket(
            inventoryId,
            stateId,
            slot.toShort(),
            buttonNum.toByte(),
            ClickType.entries[clickTypeNum],
            map,
            HashedStack.create(carriedItem?.toNMS() ?: net.minecraft.world.item.ItemStack.EMPTY, hashOpsGenerator)
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

    @Suppress("UNCHECKED_CAST")
    override fun sendPacketBundle(bundle: PacketBundle, silent: Boolean, vararg players: Player) {
        val packet = ClientboundBundlePacket(bundle.packets.map { it as Packet<ClientGamePacketListener> })
        sendPacket(packet, silent, *players)
    }

    fun Component.toNMSComponent(): net.minecraft.network.chat.Component {
        val kyoriJson = GsonComponentSerializer.gson().serialize(this)
        return ComponentSerialization.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, JsonParser.parseString(kyoriJson)).orThrow
    }
}