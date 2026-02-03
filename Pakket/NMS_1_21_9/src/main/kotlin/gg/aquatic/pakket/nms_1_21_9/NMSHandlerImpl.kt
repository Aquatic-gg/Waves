package gg.aquatic.pakket.nms_1_21_9

import com.google.common.collect.LinkedHashMultimap
import com.google.common.hash.HashCode
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import com.mojang.datafixers.util.Pair
import gg.aquatic.pakket.api.ReflectionUtils
import gg.aquatic.pakket.api.nms.NMSHandler
import gg.aquatic.pakket.api.nms.PacketEntity
import gg.aquatic.pakket.api.nms.ProtectedPacket
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.api.nms.profile.GameEventAction
import gg.aquatic.pakket.api.nms.profile.ProfileEntry
import gg.aquatic.pakket.api.nms.profile.UserProfile
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.kyori.adventure.text.Component
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.HashedPatchMap
import net.minecraft.network.HashedStack
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.RegistryOps
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import net.minecraft.stats.RecipeBookSettings
import net.minecraft.util.HashOps
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.RecipeBookType
import net.minecraft.world.item.crafting.ShapedRecipePattern
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry
import net.minecraft.world.item.crafting.display.RecipeDisplayId
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Keyed
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.CraftRegistry
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftRecipe
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerRecipeBookSettingsChangeEvent
import org.bukkit.inventory.*
import org.bukkit.util.Vector
import org.joml.Vector3d
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.jvm.optionals.getOrNull

object NMSHandlerImpl : NMSHandler() {

    private val setPassengersConstructor =
        ClientboundSetPassengersPacket::class.java.getDeclaredConstructor(FriendlyByteBuf::class.java).apply {
            isAccessible = true
        }

    override fun injectPacketListener(player: Player) {
        val craftPlayer = player as CraftPlayer
        val packetListener = PacketListener(craftPlayer)
        val connection = craftPlayer.handle.connection.connection
        addPacketListener(connection.channel, "waves_packet_listener", packetListener)
    }

    override fun unregisterPacketListener(player: Player) {
        val craftPlayer = player as CraftPlayer
        val connection = craftPlayer.handle.connection.connection
        val channel = connection.channel ?: return
        removePacketListener(channel, "waves_packet_listener")
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

    @Suppress("UNCHECKED_CAST")
    override fun createBundlePacket(packets: Collection<Any>): Any {
        val packet = ClientboundBundlePacket(
            packets.map { it as Packet<ClientGamePacketListener> }
        )
        return packet
    }

    private fun Entity.absMoveTo(x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
        this.setPos(x, y, z)
        this.yRot = yaw
        this.xRot = pitch
        this.setYBodyRot(yaw)
    }

    fun createFakePlayer(location: Location, profile: UserProfile) {
        val server = (Bukkit.getServer() as CraftServer).server
        val level = (location.world as CraftWorld).handle
        val serverPlayer =
            ServerPlayer(server, level, GameProfile(profile.uuid, profile.name), ClientInformation.createDefault())
        serverPlayer.setPos(location.x, location.y, location.z)
        serverPlayer.yRot = location.yaw

        val data = serverPlayer.entityData
        data.set(EntityDataAccessor(17, EntityDataSerializers.BYTE), 127.toByte())
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
        entity.yHeadRot = location.yaw

        val trackedEntity = worldServer.chunkSource.chunkMap.TrackedEntity(
            entity,
            50,
            50,
            true
        )
        val tracker = ServerEntity(
            worldServer,
            entity,
            entity.type.updateInterval(),
            true,
            trackedEntity,
            HashSet(),
        )
        return PacketEntity(
            location,
            entity.id,
            entity,
            entity.getAddEntityPacket(tracker),
            despawnpacket = ClientboundRemoveEntitiesPacket(entity.id)
        )
    }

    override fun createEntitySpawnPacket(
        entityId: Int,
        uuid: UUID,
        entityType: EntityType,
        pos: Vector3d,
        yaw: Float,
        pitch: Float,
    ): Any {
        return ClientboundAddEntityPacket(
            entityId,
            uuid,
            pos.x,
            pos.y,
            pos.z,
            pitch,
            yaw,
            net.minecraft.world.entity.EntityType.byString(entityType.name.lowercase()).getOrNull()!!,
            0,
            Vec3.ZERO,
            yaw.toDouble()
        )
    }

    override fun recreateEntityPacket(
        packetEntity: PacketEntity,
        location: Location,
    ): Any {
        val entity = packetEntity.entityInstance as Entity
        entity.absMoveTo(location.x, location.y, location.z, location.yaw, location.pitch)
        val worldServer = (location.world as CraftWorld).handle
        val trackedEntity = worldServer.chunkSource.chunkMap.TrackedEntity(
            entity,
            50,
            50,
            true
        )
        return entity.getAddEntityPacket(
            ServerEntity(
                worldServer,
                entity,
                entity.type.updateInterval(),
                true,
                trackedEntity,
                HashSet()
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Entity> createEntity(
        entityType: net.minecraft.world.entity.EntityType<T>,
        uuid: UUID?,
        worldServer: ServerLevel,
        blockPos: BlockPos,
    ): T? {
        val entity = if (entityType == net.minecraft.world.entity.EntityType.PLAYER) {
            val server = (Bukkit.getServer() as CraftServer).server
            val serverPlayer = ServerPlayer(
                server,
                worldServer,
                GameProfile(uuid ?: UUID.randomUUID(), "Player"),
                ClientInformation.createDefault()
            )
            serverPlayer
        } else {
            entityType.create(worldServer, EntitySpawnReason.COMMAND)
        }
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
        return entity as T?
    }

    override fun createTeleportPacket(entityId: Int, location: Location): Any {
        val container = EntityContainer(entityId)
        container.setPosRaw(location.x, location.y, location.z)
        container.setRot(location.yaw, location.pitch)
        val packet = ClientboundEntityPositionSyncPacket(entityId, PositionMoveRotation.of(container), false)
        //val packet = ClientboundTeleportEntityPacket(entityId, PositionMoveRotation.of(container), Relative.ALL, false)
        return packet
    }

    override fun createEntityUpdatePacket(id: Int, values: Collection<EntityDataValue>): Any {
        val data = values.mapNotNull { NmsMappings.mapEntityDataValue(it) }
        val packet = ClientboundSetEntityDataPacket(id, data)
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

    override fun createPassengersPacket(holderId: Int, passengerIds: IntArray): Any {
        val bytebuf = FriendlyByteBuf(Unpooled.buffer())
        bytebuf.writeVarInt(holderId)
        bytebuf.writeVarIntArray(passengerIds)

        val packet = setPassengersConstructor.newInstance(bytebuf)
        return packet
    }

    override fun createDestroyEntitiesPacket(vararg entityIds: Int): Any {
        return ClientboundRemoveEntitiesPacket(*entityIds)
    }

    override fun createPositionSyncPacket(entityId: Int, location: Location): Any {
        val container = EntityContainer(entityId)
        container.setPosRaw(location.x, location.y, location.z)
        container.setRot(location.yaw, location.pitch)
        val teleportPacket = ClientboundEntityPositionSyncPacket(
            entityId,
            PositionMoveRotation.of(container),
            container.onGround
        )
        return teleportPacket
    }

    override fun createEquipmentPacket(packetEntity: PacketEntity, equipment: Map<EquipmentSlot, ItemStack?>): Any {
        val mappedEquipment = equipment.map {
            Pair(
                net.minecraft.world.entity.EquipmentSlot.entries[it.key.ordinal],
                it.value?.let { item ->
                    NmsConversions.toNmsItemStack(item)
                } ?: net.minecraft.world.item.ItemStack.EMPTY)
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
                profileEntry.displayName?.let { NmsConversions.toNmsComponent(it) },
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

    override fun createChangeGameStatePacket(action: GameEventAction, value: Float): Any {
        val mappedAction =
            NmsMappings.gameStateTypesMapper[action]
                ?: throw IllegalArgumentException("Unknown game event action: $action")
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
            BlockPos(location.blockX, location.blockY, location.blockZ),
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

    override fun createTeamsPacket(
        team: gg.aquatic.pakket.api.nms.scoreboard.Team,
        actionId: Int,
        playerName: String,
    ): Any {
        val scoreboard = Scoreboard()
        val playerTeam = PlayerTeam(
            scoreboard,
            team.teamName
        )
        scoreboard.addPlayerTeam(team.teamName)
        playerTeam.setPlayerPrefix(NmsConversions.toNmsComponent(team.prefix))
        playerTeam.setPlayerSuffix(NmsConversions.toNmsComponent(team.suffix))
        playerTeam.collisionRule = Team.CollisionRule.entries[team.collisionRule.ordinal]
        playerTeam.nameTagVisibility =
            Team.Visibility.entries[team.nametagVisibility.ordinal]
        playerTeam.color = ChatFormatting.valueOf(team.nameColor.toString().uppercase())

        val packet = when (actionId) {
            0 -> { // CREATE_TEAM
                if (playerName.isNotEmpty()) {
                    playerTeam.players.add(playerName)
                }
                ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, true)
            }

            1 -> // REMOVE_TEAM
                ClientboundSetPlayerTeamPacket.createRemovePacket(playerTeam)

            2 -> // UPDATE_TEAM
                ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, false)

            3 -> { // ADD_PLAYER
                ClientboundSetPlayerTeamPacket.createPlayerPacket(
                    playerTeam,
                    playerName,
                    ClientboundSetPlayerTeamPacket.Action.ADD
                )
            }

            4 -> // REMOVE_PLAYER
                ClientboundSetPlayerTeamPacket.createPlayerPacket(
                    playerTeam,
                    playerName,
                    ClientboundSetPlayerTeamPacket.Action.REMOVE
                )

            else -> throw IllegalArgumentException("Invalid team action ID: $actionId")
        }
        return packet
    }

    override fun createRecipeBookAddPacket(
        id: Int,
        recipe: Recipe,
        showNotifications: Boolean,
        highlight: Boolean,
        replace: Boolean
    ): Any {
        val (nmsRecipe, ingredients) = when (recipe) {
            is ShapedRecipe -> {
                recipe.choiceMap
                val ingredients = recipe.choiceMap.mapValues {
                    CraftRecipe.toIngredient(it.value, false)
                }

                val pattern = ShapedRecipePattern.of(ingredients, recipe.shape.toList())

                net.minecraft.world.item.crafting.ShapedRecipe(
                    recipe.group,
                    CraftRecipe.getCategory(recipe.category),
                    pattern,
                    NmsConversions.toNmsItemStack(recipe.result)
                ) to ingredients.values
            }

            is ShapelessRecipe -> {
                val ingredients = recipe.choiceList.map {
                    CraftRecipe.toIngredient(it, false)
                }
                net.minecraft.world.item.crafting.ShapelessRecipe(
                    recipe.group,
                    CraftRecipe.getCategory(recipe.category),
                    NmsConversions.toNmsItemStack(recipe.result),
                    ingredients
                ) to ingredients
            }

            else -> throw IllegalArgumentException("Unsupported recipe type")
        }

        val nmsRecipeDisplay = when(nmsRecipe) {
            is net.minecraft.world.item.crafting.ShapedRecipe -> {
                nmsRecipe.display().first()
            }
            is net.minecraft.world.item.crafting.ShapelessRecipe -> {
                nmsRecipe.display().first()
            }
            else -> throw IllegalArgumentException("Unsupported recipe type")
        }

        val entry = ClientboundRecipeBookAddPacket.Entry(
            RecipeDisplayEntry(
                RecipeDisplayId(id),
                nmsRecipeDisplay,
                OptionalInt.empty(),
                nmsRecipe.recipeBookCategory(),
                Optional.of(ingredients.toList())
            ),
            showNotifications,
            highlight
        )

        return ClientboundRecipeBookAddPacket(listOf(entry), replace)
    }

    override fun createRecipeBookRemovePacket(ids: Collection<Int>): Any {
        val displayIds = ids.map { RecipeDisplayId(it) }

        return ClientboundRecipeBookRemovePacket(displayIds)
    }

    override fun createRecipeBookSettingsPacket(
        type: PlayerRecipeBookSettingsChangeEvent.RecipeBookType,
        isOpen: Boolean,
        filtering: Boolean,
    ): Any {

        val nmsType = RecipeBookType.entries[type.ordinal]

        val settings = RecipeBookSettings().apply {
            this.setOpen(nmsType, isOpen)
            this.setFiltering(nmsType, filtering)
        }

        return ClientboundRecipeBookSettingsPacket(settings)
    }

    override fun getPlayerInventoryState(player: Player): Int {
        return (player as CraftPlayer).handle.inventoryMenu.stateId
    }

    override fun createSetSlotItemPacket(inventoryId: Int, stateId: Int, slot: Int, itemStack: ItemStack?): Any {
        val packet = ClientboundContainerSetSlotPacket(
            inventoryId,
            stateId,
            slot,
            itemStack?.let { NmsConversions.toNmsItemStack(it) } ?: net.minecraft.world.item.ItemStack.EMPTY
        )
        return packet
    }

    override fun createSetWindowItemsPacket(
        inventoryId: Int,
        stateId: Int,
        items: Collection<ItemStack?>,
        carriedItem: ItemStack?,
    ): Any {
        val nmsItems = NonNullList.create<net.minecraft.world.item.ItemStack>()
        nmsItems += items.map { it?.let { item -> NmsConversions.toNmsItemStack(item) } ?: net.minecraft.world.item.ItemStack.EMPTY }
        val packet = ClientboundContainerSetContentPacket(
            inventoryId,
            stateId,
            nmsItems,
            carriedItem?.let { NmsConversions.toNmsItemStack(it) } ?: net.minecraft.world.item.ItemStack.EMPTY
        )
        return packet
    }

    override fun openWindowPacket(
        inventoryId: Int,
        menuType: MenuType,
        title: Component,
    ): Any {
        val nmsType = CraftRegistry.bukkitToMinecraft<Keyed, net.minecraft.world.inventory.MenuType<*>>(
            menuType
        )

        val packet = ClientboundOpenScreenPacket(
            inventoryId,
            nmsType,
            NmsConversions.toNmsComponent(title)
        )
        return packet
    }

    override fun createContainerPropertyPacket(
        inventoryId: Int,
        property: Int,
        value: Int
    ): Any {
        return ClientboundContainerSetDataPacket(inventoryId, property, value)
    }

    override fun closeWindowPacket(inventoryId: Int): Any {
        return ClientboundContainerClosePacket(inventoryId)
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
        val registryOps: RegistryOps<HashCode> = registryAccess.createSerializationContext(HashOps.CRC32C_INSTANCE)
        val hashOpsGenerator: HashedPatchMap.HashGenerator = HashedPatchMap.HashGenerator { typedDataComponent ->
            typedDataComponent.encodeValue(registryOps).getOrThrow { string ->
                IllegalArgumentException("Failed to hash $typedDataComponent: $string")
            }.asInt()
        }

        val map = Int2ObjectOpenHashMap<HashedStack>()
        changedSlots.forEach { (key, value) ->
            val nmsItem = value?.let { item -> NmsConversions.toNmsItemStack(item) }
                ?: net.minecraft.world.item.ItemStack.EMPTY
            map[key] = HashedStack.create(nmsItem, hashOpsGenerator)
        }

        val packet = ServerboundContainerClickPacket(
            inventoryId,
            stateId,
            slot.toShort(),
            buttonNum.toByte(),
            ClickType.entries[clickTypeNum],
            map,
            HashedStack.create(
                carriedItem?.let { item -> NmsConversions.toNmsItemStack(item) }
                    ?: net.minecraft.world.item.ItemStack.EMPTY,
                hashOpsGenerator
            )
        )

        (Bukkit.getServer() as CraftServer).server.scheduleOnMain {
            for (player in players) {
                (player as CraftPlayer).handle.connection.handleContainerClick(packet)
            }
        }
    }

    override fun generateEntityId(): Int {
        return Entity.nextEntityId()
    }

    private fun Player.sendPacket(packet: Packet<*>) {
        (this as CraftPlayer).handle.connection.send(packet)
    }


    override fun sendPacket(packet: Any, silent: Boolean, vararg players: Player) {
        if (packet !is Packet<*>) return
        for (player in players) {
            if (silent) {
                val protectedPacket = ProtectedPacket(packet)

                val playerConnection = (player as CraftPlayer).handle.connection.connection
                playerConnection.channel.pipeline().write(protectedPacket)
            } else {
                player.sendPacket(packet)
            }
        }
    }

}
