package gg.aquatic.waves.nms_1_21_4

import gg.aquatic.waves.api.NMSHandler
import gg.aquatic.waves.api.ReflectionUtils
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftEntity
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

object NMSHandlerImpl : NMSHandler {

    fun registerPacketListener(player: Player) {

    }

    val entityCounterField = ReflectionUtils.getStatic<AtomicInteger>("ENTITY_COUNTER",Entity::class.java)

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