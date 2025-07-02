package gg.aquatic.waves.fake.entity.data.impl

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.entity.DataSerializerTypes
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.ItemObjectArgument
import gg.aquatic.waves.util.version.ServerVersion
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

object ItemEntityData {

    object Item: EntityData {
        override val id: String = "item"
        override val entityClass: Class<out Entity> = org.bukkit.entity.Item::class.java

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate((arguments.any(id, updater) as? AquaticItem)?.getItem() ?: return emptyList())
        }

        fun generate(itemStack: ItemStack): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_4,ServerVersion.V_1_21_1, ServerVersion.V_1_21_5, ServerVersion.V_1_21_7 -> {
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

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            ItemObjectArgument(id, null, false),
        )
    }

}