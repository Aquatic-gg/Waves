package gg.aquatic.waves.nms_1_21_4

import gg.aquatic.waves.api.NMSHandler
import gg.aquatic.waves.api.ReflectionUtils
import io.netty.buffer.Unpooled
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.network.ServerPlayerConnection
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.Mob
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger
import kotlin.jvm.optionals.getOrNull

object NMSHandlerImpl : NMSHandler {

    fun registerPacketListener(player: Player) {

    }

    val entityCounterField = ReflectionUtils.getStatic<AtomicInteger>("ENTITY_COUNTER", Entity::class.java)

    fun showEntity(location: Location, entityType: EntityType, vararg player: Player): Pair<Int, Any>? {
        val nmsEntityType =
            net.minecraft.world.entity.EntityType.byString(entityType.name.lowercase())?.getOrNull() ?: return null
        val id = generateEntityId()

        val worldServer = (location.world as CraftWorld).handle
        val entity =
            createEntity(nmsEntityType, worldServer, BlockPos(location.blockX, location.blockY, location.blockZ))
                ?: return null

        entity.absMoveTo(location.x, location.y, location.z, location.yaw, location.pitch)

        val seenBy = HashSet<ServerPlayerConnection>()
        for (p in player) {
            seenBy.add((p as CraftPlayer).handle.connection)
        }

        val tracker = ServerEntity(
            worldServer,
            entity,
            entity.type.updateInterval(),
            true,
            { },
            seenBy,
        )

        for (item in player) {
            item.sendPacket(entity.getAddEntityPacket(tracker))
        }
        /*
        val packet = ClientboundAddEntityPacket(
            id,
            UUID.randomUUID(),
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch,
            nmsEntityType,
            0,
            Vec3(0.0, 0.0, 0.0),
            location.yaw.toDouble()
        )
        for (player in player) {
            player.sendPacket(packet)
        }
         */
        return id to entity
    }

    private fun <T : Entity> createEntity(
        entityType: net.minecraft.world.entity.EntityType<T>,
        worldServer: ServerLevel,
        blockPos: BlockPos
    ): T? {
        val entity = entityType.create(worldServer, EntitySpawnReason.COMMAND)
        entity?.let {
            it.moveTo(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), 0.0f, 0.0f)
            worldServer.addFreshEntityWithPassengers(it)

            if (it is Mob) {
                it.yHeadRot = it.yRot
                it.yBodyRot = it.yRot
            }
        }
        return entity
    }

    fun updateEntityPacket(entityAny: Any, consumer: (org.bukkit.entity.Entity) -> Unit, vararg players: Player) {
        val entity = (entityAny as Entity).bukkitEntity.apply {
            consumer(this)
        }.handle

        val packet = ClientboundSetEntityDataPacket(
            entity.id,
            entity.entityData.nonDefaultValues ?: return
        )
        for (player in players) {
            player.sendPacket(packet)
        }
    }

    val setPassengersConstructor =
        ClientboundSetPassengersPacket::class.java.getConstructor(FriendlyByteBuf::class.java).apply {
            isAccessible = true
        }

    fun setPassengers(baseId: Int, passengerIds: IntArray, vararg players: Player) {
        val bytebuf = FriendlyByteBuf(Unpooled.buffer())
        bytebuf.writeVarInt(baseId)
        bytebuf.writeVarIntArray(passengerIds)

        val packet = setPassengersConstructor.newInstance(bytebuf)
        for (player in players) {
            player.sendPacket(packet)
        }
    }

    fun setSlotItem(inventoryId: Int, stateId: Int, slot: Int, itemStack: ItemStack, vararg players: Player) {
        val packet = ClientboundContainerSetSlotPacket(inventoryId, stateId, slot, itemStack.toNMS())

        for (player in players) {
            player.sendPacket(packet)
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
}