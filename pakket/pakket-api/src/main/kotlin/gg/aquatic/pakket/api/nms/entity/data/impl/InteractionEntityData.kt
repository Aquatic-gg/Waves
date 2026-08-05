package gg.aquatic.pakket.api.nms.entity.data.impl

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArguments
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.pakket.api.NMSVersion
import gg.aquatic.pakket.api.nms.entity.DataSerializerTypes
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.api.nms.entity.data.EntityData
import org.bukkit.entity.Interaction

object InteractionEntityData: BaseEntityData() {

    object Width: EntityData {
        override val id: String = "width"
        override val entityClass: Class<out Interaction> = Interaction::class.java

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.float(id, updater) ?: 1f)
        }
        fun generate(width: Float): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.create(
                            8,
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
            PrimitiveObjectArgument(id, 1f, false),
        )
    }
    object Height: EntityData {
        override val id: String = "height"
        override val entityClass: Class<out Interaction> = Interaction::class.java

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.float(id, updater) ?: 1f)
        }
        fun generate(height: Float): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.create(
                            9,
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
            PrimitiveObjectArgument(id, 1f, false),
        )
    }
    object Responsive: EntityData {
        override val id: String = "responsive"
        override val entityClass: Class<out Interaction> = Interaction::class.java

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.boolean(id, updater) ?: true)
        }
        fun generate(responsive: Boolean): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.create(
                            10,
                            DataSerializerTypes.BOOLEAN,
                            responsive
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, defaultValue = true, required = false),
        )
    }

}