package gg.aquatic.pakket.api.nms.entity

import gg.aquatic.pakket.api.nms.BlockPos
import gg.aquatic.pakket.api.nms.Direction
import net.kyori.adventure.text.Component
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Pose
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*

object DataSerializerTypes {

    val BYTE = createSerializerType<Byte>()
    val INT = createSerializerType<Int>()
    val LONG = createSerializerType<Long>()
    val FLOAT = createSerializerType<Float>()
    val STRING = createSerializerType<String>()
    val COMPONENT = createSerializerType<Component>()
    val OPTIONAL_COMPONENT = createSerializerType<Optional<Component>>()
    val ITEM_STACK = createSerializerType<ItemStack>()
    val BLOCK_STATE = createSerializerType<BlockData>()
    val OPTIONAL_BLOCK_STATE = createSerializerType<Optional<BlockData>>()
    val BOOLEAN = createSerializerType<Boolean>()
    val BLOCK_POS = createSerializerType<BlockPos>()
    val OPTIONAL_BLOCK_POS = createSerializerType<Optional<BlockPos>>()
    val DIRECTION = createSerializerType<Direction>()
    val OPTIONAL_UUID = createSerializerType<Optional<UUID>>()
    val OPTIONAL_UNSIGNED_INT = createSerializerType<Optional<Int>>()
    val POSE = createSerializerType<Pose>()
    val VECTOR3 = createSerializerType<Vector3f>()
    val QUATERNION = createSerializerType<Quaternionf>()
    val ROTATIONS = createSerializerType<Vector>()


    private fun <T: Any> createSerializerType(): DataSerializerType<T> =
        object : DataSerializerType<T> {}

    interface DataSerializerType<T: Any>
}