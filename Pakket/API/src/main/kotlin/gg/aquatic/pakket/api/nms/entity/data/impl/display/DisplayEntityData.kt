package gg.aquatic.pakket.api.nms.entity.data.impl.display

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArguments
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.pakket.api.NMSVersion
import gg.aquatic.pakket.api.nms.entity.DataSerializerTypes
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.api.nms.entity.data.EntityData
import gg.aquatic.pakket.api.nms.entity.data.impl.living.BaseEntityData
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.joml.Quaternionf
import org.joml.Vector3f

open class DisplayEntityData internal constructor(): BaseEntityData() {

    abstract class Base(): EntityData {
        override val entityClass: Class<out Entity> = Display::class.java
    }

    object InterpolationDelay : Base() {
        override val id: String = "interpolation-delay"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.int(id, updater) ?: 0)
        }

        fun generate(interpolationDelay: Int): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            8,
                            DataSerializerTypes.INT,
                            interpolationDelay
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, "0", false),
        )
    }

    object TransformationInterpolationDuration : Base() {
        override val id: String = "interpolation-duration"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.int(id, updater) ?: 0)
        }

        fun generate(interpolationDuration: Int): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            9,
                            DataSerializerTypes.INT,
                            interpolationDuration
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, "0", false),
        )
    }

    object TeleportationDuration : Base() {
        override val id: String = "teleportation-duration"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.int(id, updater) ?: 0)
        }

        fun generate(teleportationDuration: Int): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            10,
                            DataSerializerTypes.INT,
                            teleportationDuration
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, "0", false),
        )
    }

    object Translation : Base() {
        override val id: String = "translation"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.vector3f(id, updater) ?: Vector3f())
        }

        fun generate(translation: Vector3f): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9  -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            11,
                            DataSerializerTypes.VECTOR3,
                            translation
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, "0;0;0", false),
        )
    }

    object Scale : Base() {
        override val id: String = "scale"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.vector3f(id, updater) ?: Vector3f(1f, 1f, 1f))
        }

        fun generate(scale: Vector3f): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            12,
                            DataSerializerTypes.VECTOR3,
                            scale
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, "1;1;1", false),
        )
    }

    object Rotation : Base() {
        override val id: String = "rotation"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.vector3f(id, updater) ?: Vector3f(0f, 0f, 0f))
        }

        fun generate(rotation: Vector3f): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            13,
                            DataSerializerTypes.QUATERNION,
                            Quaternionf().rotateXYZ(rotation.x, rotation.y, rotation.z)
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, "0;0;0", false),
        )
    }

    object RightRotation : Base() {
        override val id: String = "right-rotation"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.vector3f(id, updater) ?: Vector3f(0f, 0f, 0f))
        }

        fun generate(rotation: Vector3f): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            14,
                            DataSerializerTypes.QUATERNION,
                            Quaternionf().rotateXYZ(rotation.x, rotation.y, rotation.z)
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, "0;0;0", false),
        )
    }

    object Billboard : Base() {
        override val id: String = "billboard"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(
                arguments.enum<Display.Billboard>(id, updater)
                    ?: Display.Billboard.FIXED
            )
        }

        fun generate(billboard: Display.Billboard): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            15,
                            DataSerializerTypes.BYTE,
                            billboard.ordinal.toByte()
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, "FIXED", false),
        )
    }

    object Brightness : Base() {
        override val id: String = "brightness"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            val blockLight = arguments.int("$id.block-light", updater) ?: 0
            val skyLight = arguments.int("$id.sky-light", updater) ?: 0
            return generate(blockLight, skyLight)
        }

        fun generate(blockLight: Int, skyLight: Int): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {

                    val packedLight = (blockLight shl 4) or (skyLight shl 20)
                    return listOf(
                        EntityDataValue.Companion.create(
                            16,
                            DataSerializerTypes.BYTE,
                            packedLight.toByte()
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        fun getLight(byte: Byte): Pair<Int, Int> {
            val blockLight = byte.toInt() shr 4 and 0xF
            val skyLight = byte.toInt() shr 20 and 0xF
            return Pair(blockLight, skyLight)
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument("$id.block-light", "0", false),
            PrimitiveObjectArgument("$id.sky-light", "0", false),
        )
    }

    object ViewRange : Base() {
        override val id: String = "view-range"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.float(id, updater) ?: 100f)
        }

        fun generate(viewRange: Float): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            17,
                            DataSerializerTypes.FLOAT,
                            viewRange
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, 100f, false),
        )
    }

    object ShadowRadius : Base() {
        override val id: String = "shadow-radius"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.float(id, updater) ?: 0f)
        }

        fun generate(shadowRadius: Float): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            18,
                            DataSerializerTypes.FLOAT,
                            shadowRadius
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, 0f, false),
        )
    }

    object ShadowStrength : Base() {
        override val id: String = "shadow-strength"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.float(id, updater) ?: 1f)
        }

        fun generate(shadowStrength: Float): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            19,
                            DataSerializerTypes.FLOAT,
                            shadowStrength
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, 1f, false),
        )
    }

    object Width : Base() {
        override val id: String = "width"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.float(id, updater) ?: 0f)
        }

        fun generate(width: Float): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            20,
                            DataSerializerTypes.FLOAT,
                            width
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, 0f, false),
        )
    }

    object Height : Base() {
        override val id: String = "height"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.float(id, updater) ?: 0f)
        }

        fun generate(height: Float): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.Companion.create(
                            21,
                            DataSerializerTypes.FLOAT,
                            height
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, 0f, false),
        )
    }
}