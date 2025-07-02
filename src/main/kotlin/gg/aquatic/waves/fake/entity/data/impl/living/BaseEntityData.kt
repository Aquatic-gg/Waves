package gg.aquatic.waves.fake.entity.data.impl.living

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.entity.DataSerializerTypes
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.toMMComponent
import gg.aquatic.waves.util.version.ServerVersion
import net.kyori.adventure.text.Component
import org.bukkit.entity.Entity
import java.util.*

open class BaseEntityData internal constructor() {

    abstract class Base: EntityData {
        override val entityClass: Class<out Entity> = Entity::class.java
    }

    object Visuals: Base() {
        override val id: String = "visuals"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            val isOnFire = arguments.boolean("$id.is-on-fire", updater) ?: false
            val isSneaking = arguments.boolean("$id.is-sneaking", updater) ?: false
            val isSprinting = arguments.boolean("$id.is-sprinting", updater) ?: false
            val isSwimming = arguments.boolean("$id.is-swimming", updater) ?: false
            val isInvisible = arguments.boolean("$id.is-invisible", updater) ?: false
            val isGlowing = arguments.boolean("$id.is-glowing", updater) ?: false
            val isElytraFlying = arguments.boolean("$id.is-elytra-flying", updater) ?: false

            return generate(
                isOnFire,
                isSneaking,
                isSprinting,
                isSwimming,
                isInvisible,
                isGlowing,
                isElytraFlying,
            )
        }

        fun generate(
            isOnFire: Boolean,
            isSneaking: Boolean,
            isSprinting: Boolean,
            isSwimming: Boolean,
            isInvisible: Boolean,
            isGlowing: Boolean,
            isElytraFlying: Boolean,
        ): Collection<EntityDataValue> {


            val packedByte = packEntityFlags(
                isOnFire,
                isSneaking,
                isSprinting,
                isSwimming,
                isInvisible,
                isGlowing,
                isElytraFlying
            )

            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_4,ServerVersion.V_1_21_1,ServerVersion.V_1_21_5, ServerVersion.V_1_21_7 -> {
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
            isOnFire: Boolean = false,
            isSneaking: Boolean = false,
            isSprinting: Boolean = false,
            isSwimming: Boolean = false,
            isInvisible: Boolean = false,
            isGlowing: Boolean = false,
            isElytraFlying: Boolean = false
        ): Byte {
            var result: Byte = 0

            // Apply each flag with its corresponding bit mask
            if (isOnFire) result = (0 or 0x01).toByte()
            if (isSneaking) result = (result.toInt() or 0x02).toByte()
            if (isSprinting) result = (result.toInt() or 0x08).toByte()
            if (isSwimming) result = (result.toInt() or 0x10).toByte()
            if (isInvisible) result = (result.toInt() or 0x20).toByte()
            if (isGlowing) result = (result.toInt() or 0x40).toByte()
            if (isElytraFlying) result = (result.toInt() or 0x80).toByte()

            return result
        }


        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument("$id.is-on-fire", false, required = false),
            PrimitiveObjectArgument("$id.is-sneaking", false, required = false),
            PrimitiveObjectArgument("$id.is-sprinting", false, required = false),
            PrimitiveObjectArgument("$id.is-swimming", false, required = false),
            PrimitiveObjectArgument("$id.is-invisible", false, required = false),
            PrimitiveObjectArgument("$id.is-glowing", false, required = false),
            PrimitiveObjectArgument("$id.is-elytra-flying", false, required = false),
        )
    }

    object AirTicks: Base() {
        override val id: String = "air-ticks"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.int(id, updater) ?: 300)
        }

        fun generate(airTicks: Int): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_4,ServerVersion.V_1_21_1,ServerVersion.V_1_21_5, ServerVersion.V_1_21_7 -> {
                    return listOf(
                        EntityDataValue.create(
                            1,
                            DataSerializerTypes.INT,
                            airTicks
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, 300, false),
        )
    }

    object CustomName: Base() {
        override val id: String = "custom-name"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(Optional.ofNullable(arguments.string(id, updater)?.toMMComponent()))
        }

        fun generate(customName: Optional<Component>): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_1,ServerVersion.V_1_21_4,ServerVersion.V_1_21_5, ServerVersion.V_1_21_7 -> {
                    return listOf(
                        EntityDataValue.create(
                            2,
                            DataSerializerTypes.OPTIONAL_COMPONENT,
                            customName
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, null, false),
        )
    }

    object CustomNameVisible: Base() {
        override val id: String = "custom-name-visible"
        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.boolean(id, updater) ?: false)
        }

        fun generate(isVisible: Boolean): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_4,ServerVersion.V_1_21_1,ServerVersion.V_1_21_5, ServerVersion.V_1_21_7 -> {
                    return listOf(
                        EntityDataValue.create(
                            3,
                            DataSerializerTypes.BOOLEAN,
                            isVisible
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, defaultValue = false, required = false),
        )
    }
    object Silent: Base() {
        override val id: String = "silent"
        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.boolean(id, updater) ?: false)
        }

        fun generate(silent: Boolean): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_4,ServerVersion.V_1_21_1,ServerVersion.V_1_21_5, ServerVersion.V_1_21_7 -> {
                    return listOf(
                        EntityDataValue.create(
                            4,
                            DataSerializerTypes.BOOLEAN,
                            silent
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, defaultValue = false, required = false),
        )
    }
    object HasGravity: Base() {
        override val id: String = "has-gravity"
        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.boolean(id, updater) ?: false)
        }

        fun generate(hasGravity: Boolean): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_4,ServerVersion.V_1_21_1,ServerVersion.V_1_21_5, ServerVersion.V_1_21_7 -> {
                    return listOf(
                        EntityDataValue.create(
                            5,
                            DataSerializerTypes.BOOLEAN,
                            !hasGravity
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, defaultValue = false, required = false),
        )
    }
    object Pose: Base() {
        override val id: String = "pose"
        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.enum<org.bukkit.entity.Pose>(id, updater) ?: org.bukkit.entity.Pose.STANDING)
        }

        fun generate(pose: org.bukkit.entity.Pose): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_4,ServerVersion.V_1_21_1,ServerVersion.V_1_21_5, ServerVersion.V_1_21_7 -> {
                    return listOf(
                        EntityDataValue.create(
                            6,
                            DataSerializerTypes.POSE,
                            pose
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, defaultValue = null, required = false),
        )
    }

    object FrozenTicks: Base() {
        override val id: String = "frozen-ticks"
        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.int(id, updater) ?: 0)
        }

        fun generate(ticks: Int): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_4,ServerVersion.V_1_21_1,ServerVersion.V_1_21_5, ServerVersion.V_1_21_7 -> {
                    return listOf(
                        EntityDataValue.create(
                            7,
                            DataSerializerTypes.INT,
                            ticks
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, defaultValue = 0, required = false),
        )
    }

}