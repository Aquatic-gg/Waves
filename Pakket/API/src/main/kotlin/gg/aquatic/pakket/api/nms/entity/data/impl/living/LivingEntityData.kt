package gg.aquatic.pakket.api.nms.entity.data.impl.living

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArguments
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.pakket.api.NMSVersion
import gg.aquatic.pakket.api.nms.BlockPos
import gg.aquatic.pakket.api.nms.entity.DataSerializerTypes
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.api.nms.entity.data.EntityData
import gg.aquatic.pakket.api.nms.entity.data.impl.BaseEntityData
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.*

open class LivingEntityData internal constructor(): BaseEntityData() {

    abstract class Base: EntityData {
        override val entityClass: Class<out Entity> = LivingEntity::class.java
    }

    object Hand: Base() {
        override val id: String = "hand"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            val isActive = arguments.boolean("$id.is-active", updater) ?: false
            val isOffHand = arguments.boolean("$id.active-hand", updater) ?: false
            val isRiptideAttack = arguments.boolean("$id.is-riptide-attack", updater) ?: false


            return generate(
                isActive,
                isOffHand,
                isRiptideAttack
            )
        }

        fun generate(
            isActive: Boolean,
            isOffHand: Boolean,
            isRiptideAttack: Boolean,
        ): Collection<EntityDataValue> {


            val packedByte = packEntityFlags(
                isActive,
                isOffHand,
                isRiptideAttack
            )

            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.create(
                            0,
                            DataSerializerTypes.BYTE,
                            packedByte
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        private fun packEntityFlags(
            isActive: Boolean = false,
            isOffHand: Boolean = false,
            isRiptideAttack: Boolean = false,
        ): Byte {
            var result: Byte = 0

            // Apply each flag with its corresponding bit mask
            if (isActive) result = (0 or 0x01).toByte()
            if (isOffHand) result = (result.toInt() or 0x02).toByte()
            if (isRiptideAttack) result = (result.toInt() or 0x04).toByte()

            return result
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument("$id.is-active", false, required = false),
            PrimitiveObjectArgument("$id.active-hand", false, required = false),
            PrimitiveObjectArgument("$id.is-riptide-attack", false, required = false),
        )
    }

    object Health: Base() {
        override val id: String = "health"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.float(id, updater) ?: 1.0f)
        }

        fun generate(health: Float): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.create(
                            9,
                            DataSerializerTypes.FLOAT,
                            health
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, 1.0f, false),
        )
    }

    object SleepingPosition: Base() {
        override val id: String = "sleeping-position"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            val blockPos = arguments.vector(id, updater)?.let {
                BlockPos(it.blockX, it.blockY, it.blockZ)
            }

            return generate(Optional.ofNullable(blockPos))
        }

        fun generate(position: Optional<BlockPos>): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.create(
                            9,
                            DataSerializerTypes.OPTIONAL_BLOCK_POS,
                            position
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, 1.0f, false),
        )
    }
}