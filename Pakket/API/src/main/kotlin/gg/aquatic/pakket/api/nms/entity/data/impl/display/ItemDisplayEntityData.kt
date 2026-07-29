package gg.aquatic.pakket.api.nms.entity.data.impl.display

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArguments
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.pakket.api.NMSVersion
import gg.aquatic.pakket.api.nms.entity.DataSerializerTypes
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.api.nms.entity.data.EntityData
import gg.aquatic.stacked.argument.ItemObjectArgument
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack

object ItemDisplayEntityData: DisplayEntityData() {

    abstract class Base: EntityData {
        override val entityClass: Class<out ItemDisplay> = ItemDisplay::class.java
    }

    object Item: Base() {
        override val id: String = "display-item"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.any(id, updater) as? ItemStack ?: return emptyList())
        }

        fun generate(item: ItemStack): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.create(
                            23,
                            DataSerializerTypes.ITEM_STACK,
                            item
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            ItemObjectArgument(id, null, false),
        )
    }
    object ItemDisplayTransform: Base() {
        override val id: String = "item-display-transform"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.enum<ItemDisplay.ItemDisplayTransform>(id, updater) ?: return emptyList())
        }

        fun generate(display: ItemDisplay.ItemDisplayTransform): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.create(
                            24,
                            DataSerializerTypes.BYTE,
                            display.ordinal.toByte()
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<ObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, "GROUND", false),
        )
    }

}