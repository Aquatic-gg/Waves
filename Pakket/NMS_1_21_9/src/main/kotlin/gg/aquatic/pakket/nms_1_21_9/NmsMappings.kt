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
import kotlin.jvm.optionals.getOrNull

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

    @Suppress("UNCHECKED_CAST")
    fun mapEntityDataValue(original: EntityDataValue): SynchedEntityData.DataValue<*>? {
        return when (original.serializerType) {
            DataSerializerTypes.BYTE -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.BYTE,
                original.value as Byte
            )

            DataSerializerTypes.INT -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.INT,
                original.value as Int
            )

            DataSerializerTypes.FLOAT -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.FLOAT,
                original.value as Float
            )

            DataSerializerTypes.STRING -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.STRING,
                original.value as String
            )

            DataSerializerTypes.BOOLEAN -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.BOOLEAN,
                original.value as Boolean
            )

            DataSerializerTypes.OPTIONAL_COMPONENT -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.OPTIONAL_COMPONENT,
                (original.value as Optional<Component>).getOrNull().let {
                    val nmsComponent = it?.let { component -> NmsConversions.toNmsComponent(component) }
                    Optional.ofNullable(nmsComponent)
                }
            )

            DataSerializerTypes.ITEM_STACK -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.ITEM_STACK,
                CraftItemStack.asNMSCopy(original.value as ItemStack)
            )

            DataSerializerTypes.ROTATIONS -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.ROTATIONS,
                (original.value as Vector).let {
                    Rotations(it.x.toFloat(), it.y.toFloat(), it.z.toFloat())
                }
            )

            DataSerializerTypes.BLOCK_POS -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.BLOCK_POS,
                (original.value as BlockPos).let { BlockPos(it.x, it.y, it.z) }
            )

            DataSerializerTypes.BLOCK_STATE -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.BLOCK_STATE,
                (original.value as CraftBlockData).state
            )

            DataSerializerTypes.COMPONENT -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.COMPONENT,
                NmsConversions.toNmsComponent(original.value as Component)
            )

            DataSerializerTypes.LONG -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.LONG,
                original.value as Long
            )

            DataSerializerTypes.POSE -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.POSE,
                (original.value as Pose).let {
                    net.minecraft.world.entity.Pose.entries[it.ordinal]
                }
            )

            DataSerializerTypes.VECTOR3 -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.VECTOR3,
                original.value as Vector3f
            )

            DataSerializerTypes.DIRECTION -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.DIRECTION,
                (original.value as Direction).let {
                    Direction.entries[it.ordinal]
                }
            )

            DataSerializerTypes.OPTIONAL_BLOCK_POS -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.OPTIONAL_BLOCK_POS,
                (original.value as Optional<BlockPos>).let {
                    Optional.ofNullable(it.getOrNull()?.let { pos -> BlockPos(pos.x, pos.y, pos.z) })
                }
            )

            DataSerializerTypes.OPTIONAL_BLOCK_STATE -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.OPTIONAL_BLOCK_STATE,
                (original.value as Optional<BlockData>).let {
                    Optional.ofNullable(it.getOrNull()?.let { blockData -> (blockData as CraftBlockData).state })
                }
            )

            DataSerializerTypes.QUATERNION -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.QUATERNION,
                original.value as Quaternionf
            )

            DataSerializerTypes.OPTIONAL_UNSIGNED_INT -> SynchedEntityData.DataValue(
                original.id,
                EntityDataSerializers.OPTIONAL_UNSIGNED_INT,
                (original.value as Optional<Int>).getOrNull().let {
                    if (it == null) OptionalInt.empty() else OptionalInt.of(it)
                }
            )

            else -> null
        }
    }
}
