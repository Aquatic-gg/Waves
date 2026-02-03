package gg.aquatic.pakket.nms_1_21_9

import gg.aquatic.pakket.api.nms.entity.DataSerializerTypes
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.api.nms.profile.GameEventAction
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Rotations
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Pose
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.Optional
import java.util.OptionalInt

object NmsMappings {

    val gameStateTypesMapper = hashMapOf(
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

    private fun <T: Any> dataValue(
        original: EntityDataValue,
        serializer: net.minecraft.network.syncher.EntityDataSerializer<T>,
        value: T
    ) = SynchedEntityData.DataValue(original.id, serializer, value)

    private fun <T, R : Any> optionalDataValue(
        original: EntityDataValue,
        serializer: net.minecraft.network.syncher.EntityDataSerializer<Optional<R>>,
        value: Optional<T>,
        mapper: (T) -> R
    ): SynchedEntityData.DataValue<Optional<R>> {
        val mapped = if (value.isPresent) Optional.of<R>(mapper(value.get())) else Optional.empty<R>()
        return dataValue(original, serializer, mapped)
    }

    @Suppress("UNCHECKED_CAST")
    fun mapEntityDataValue(original: EntityDataValue): SynchedEntityData.DataValue<*>? {
        return when (original.serializerType) {
            DataSerializerTypes.BYTE ->
                dataValue(original, EntityDataSerializers.BYTE, original.value as Byte)

            DataSerializerTypes.INT ->
                dataValue(original, EntityDataSerializers.INT, original.value as Int)

            DataSerializerTypes.FLOAT ->
                dataValue(original, EntityDataSerializers.FLOAT, original.value as Float)

            DataSerializerTypes.STRING ->
                dataValue(original, EntityDataSerializers.STRING, original.value as String)

            DataSerializerTypes.BOOLEAN ->
                dataValue(original, EntityDataSerializers.BOOLEAN, original.value as Boolean)

            DataSerializerTypes.OPTIONAL_COMPONENT ->
                optionalDataValue(
                    original,
                    EntityDataSerializers.OPTIONAL_COMPONENT,
                    original.value as Optional<Component>,
                ) { component -> NmsConversions.toNmsComponent(component) }

            DataSerializerTypes.ITEM_STACK ->
                dataValue(
                    original,
                    EntityDataSerializers.ITEM_STACK,
                    CraftItemStack.asNMSCopy(original.value as ItemStack)
                )

            DataSerializerTypes.ROTATIONS ->
                dataValue(
                    original,
                    EntityDataSerializers.ROTATIONS,
                    (original.value as Vector).let { Rotations(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }
                )

            DataSerializerTypes.BLOCK_POS ->
                dataValue(
                    original,
                    EntityDataSerializers.BLOCK_POS,
                    (original.value as BlockPos).let { BlockPos(it.x, it.y, it.z) }
                )

            DataSerializerTypes.BLOCK_STATE ->
                dataValue(original, EntityDataSerializers.BLOCK_STATE, (original.value as CraftBlockData).state)

            DataSerializerTypes.COMPONENT ->
                dataValue(
                    original,
                    EntityDataSerializers.COMPONENT,
                    NmsConversions.toNmsComponent(original.value as Component)
                )

            DataSerializerTypes.LONG ->
                dataValue(original, EntityDataSerializers.LONG, original.value as Long)

            DataSerializerTypes.POSE ->
                dataValue(
                    original,
                    EntityDataSerializers.POSE,
                    Pose.entries[(original.value as Pose).ordinal]
                )

            DataSerializerTypes.VECTOR3 ->
                dataValue(original, EntityDataSerializers.VECTOR3, original.value as Vector3f)

            DataSerializerTypes.DIRECTION ->
                dataValue(
                    original,
                    EntityDataSerializers.DIRECTION,
                    Direction.entries[(original.value as Direction).ordinal]
                )

            DataSerializerTypes.OPTIONAL_BLOCK_POS ->
                optionalDataValue(
                    original,
                    EntityDataSerializers.OPTIONAL_BLOCK_POS,
                    original.value as Optional<BlockPos>
                ) { pos -> BlockPos(pos.x, pos.y, pos.z) }

            DataSerializerTypes.OPTIONAL_BLOCK_STATE ->
                optionalDataValue(
                    original,
                    EntityDataSerializers.OPTIONAL_BLOCK_STATE,
                    original.value as Optional<BlockData>
                ) { blockData -> (blockData as CraftBlockData).state }

            DataSerializerTypes.QUATERNION ->
                dataValue(original, EntityDataSerializers.QUATERNION, original.value as Quaternionf)

            DataSerializerTypes.OPTIONAL_UNSIGNED_INT ->
                dataValue(
                    original,
                    EntityDataSerializers.OPTIONAL_UNSIGNED_INT,
                    (original.value as Optional<Int>).let { value ->
                        if (value.isPresent) OptionalInt.of(value.get()) else OptionalInt.empty()
                    }
                )

            else -> null
        }
    }
}
