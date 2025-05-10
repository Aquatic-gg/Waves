package gg.aquatic.waves.util

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.NMSHandler
import gg.aquatic.waves.api.nms.PacketEntity
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/*
fun Entity.getEntityData(): List<EntityData> {
    val nmsEntity = SpigotReflectionUtil.getNMSEntity(this)

    val synchedDataMethod = SpigotReflectionUtil.NMS_ENTITY_CLASS.getDeclaredMethod("ar")
    val synchedData = synchedDataMethod.invoke(nmsEntity)
    Bukkit.broadcastMessage("SynchedData: ${synchedData.javaClass.name}")
    val listField = synchedData.javaClass.getDeclaredField("e")
    Bukkit.broadcastMessage("Data List: ${listField.name}")
    listField.isAccessible = true
    val list = listField.get(synchedData) as Array<*>
    Bukkit.broadcastMessage("List: ${list.size}")

    val buffer = PooledByteBufAllocator.DEFAULT.buffer()
    val registryByteBuf = SpigotReflectionUtil.createPacketDataSerializer(buffer)
    for (entry in list) {
        val valueMethod = entry?.javaClass?.getDeclaredMethod("e") ?: continue
        val value = valueMethod.invoke(entry) ?: continue
        val writeMethod = value.javaClass.getDeclaredMethod("a", registryByteBuf.javaClass)
        writeMethod.invoke(
            value,
            registryByteBuf
        )
    }
    ByteBufHelper.writeByte(buffer, 255)
    val entityData = readEntityMetadata(buffer)
    Bukkit.broadcastMessage("Values: ${entityData.size}")
    return entityData
}
fun readEntityMetadata(buffer: Any): List<EntityData> {
    val wrapper = PacketWrapper(WrapperPlayServerEntityMetadata(0, mutableListOf()).nativePacketId)
    wrapper.setBuffer(buffer)
    val data = wrapper.readEntityMetadata()
    return data
}
 */
/*
fun Entity.setEntityData(data: List<EntityData>) {
    val nmsEntity = SpigotReflectionUtil.getNMSEntity(this)
    val synchedDataMethod = SpigotReflectionUtil.NMS_ENTITY_CLASS.getDeclaredMethod("ar")
    val synchedData = synchedDataMethod.invoke(nmsEntity)
    val wrapper = PacketWrapper(WrapperPlayServerEntityMetadata(0, mutableListOf()).nativePacketId)
    wrapper.setBuffer(PooledByteBufAllocator.DEFAULT.buffer())
    wrapper.writeEntityMetadata(data)
    for (method in synchedData.javaClass.declaredMethods) {
        Bukkit.broadcastMessage("Declared Method: ${method.name}, ${method.parameterCount}")
    }
    for (method in synchedData.javaClass.methods) {
        Bukkit.broadcastMessage("Method: ${method.name}, ${method.parameterCount}")
    }
    val unpackMethod = synchedData.javaClass.getMethod("unpack", SpigotReflectionUtil.NMS_PACKET_DATA_SERIALIZER_CLASS)
    val nmsData = unpackMethod.invoke(synchedData, wrapper.getBuffer())
    val assignMethod = synchedData.javaClass.getMethod("a", List::class.java)
    assignMethod.invoke(synchedData, nmsData)
}
 */


fun PacketEntity.modify(consumer: (Entity) -> Unit) {
    val packet = Waves.NMS_HANDLER.createEntityUpdatePacket(this,consumer)
    this.updatePacket = packet
}
fun PacketEntity.setPassengers(ids: IntArray) {
    val packet = Waves.NMS_HANDLER.createPassengersPacket(this,ids)
    this.updatePacket = packet
}
fun PacketEntity.setPassengers(vararg entities: Entity) {
    entities.map { it.entityId }.toIntArray().let {
        this.setPassengers(it)
    }
}
fun PacketEntity.setEquipment(equipment: Map<EquipmentSlot, ItemStack>) {
    //val packet = Waves.NMS_HANDLER.createEquipmentPacket(this, equipment)
    this.equipment += equipment
}

fun Player.sendPacket(packet: Any, silent: Boolean = false) {
    Waves.NMS_HANDLER.sendPacket(packet, silent, this)
}
