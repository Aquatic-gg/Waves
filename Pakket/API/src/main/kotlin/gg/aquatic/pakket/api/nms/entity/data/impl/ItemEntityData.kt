package gg.aquatic.pakket.api.nms.entity.data.impl

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArguments
import gg.aquatic.pakket.api.NMSVersion
import gg.aquatic.pakket.api.nms.entity.DataSerializerTypes
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.api.nms.entity.data.EntityData
import gg.aquatic.stacked.StackedItem
import gg.aquatic.stacked.argument.ItemObjectArgument
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

object ItemEntityData {

    object Item: EntityData {
        override val id: String = "item"
        override val entityClass: Class<out Entity> = org.bukkit.entity.Item::class.java

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate((arguments.any(id, updater) as? StackedItem)?.getItem() ?: return emptyList())
        }
        fun generate(itemStack: ItemStack): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.create(
                            8,
                            DataSerializerTypes.ITEM_STACK,
                            itemStack
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

}