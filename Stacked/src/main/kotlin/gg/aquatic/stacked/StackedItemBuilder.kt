package gg.aquatic.stacked

import gg.aquatic.stacked.impl.StackedItemImpl
import gg.aquatic.stacked.option.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack

@DslMarker
annotation class StackedDsl

@StackedDsl
class StackedItemBuilder(private val baseStack: ItemStack) {

    private val options = mutableMapOf<Class<out ItemOptionHandle>, ItemOptionHandle>()

    var amount: Int = 1
        set(value) {
            options[AmountOptionHandle::class.java] = AmountOptionHandle(value)
            field = value
        }

    var displayName: Component? = null
        set(value) {
            value?.let { options[DisplayNameOptionHandle::class.java] = DisplayNameOptionHandle(it) }
            field = value
        }

    val lore = mutableListOf<Component>()

    var customModelDataLegacy: Int? = null
        set(value) {
            value?.let {
                options[CustomModelDataLegacyOptionHandle::class.java] = CustomModelDataLegacyOptionHandle(it)
            }
            field = value
        }

    fun customModelData(builder: CustomModelDataBuilder.() -> Unit) {
        val dataBuilder = CustomModelDataBuilder().apply(builder)
        options[CustomModelDataOptionHandle::class.java] = dataBuilder.build()
    }

    var itemModel: Key? = null
        set(value) {
            value?.let { options[ItemModelOptionHandle::class.java] = ItemModelOptionHandle(it) }
            field = value
        }

    var damage: Int? = null
        set(value) {
            value?.let { options[DamageOptionHandle::class.java] = DamageOptionHandle(it) }
            field = value
        }

    var maxDamage: Int? = null
        set(value) {
            value?.let { options[MaxDamageOptionHandle::class.java] = MaxDamageOptionHandle(it) }
            field = value
        }

    var maxStackSize: Int? = null
        set(value) {
            value?.let { options[MaxStackSizeOptionHandle::class.java] = MaxStackSizeOptionHandle(it) }
            field = value
        }

    var unbreakable: Boolean = false
        set(value) {
            if (!value) {
                options -= UnbreakableOptionHandle::class.java
            } else {
                options[UnbreakableOptionHandle::class.java] = UnbreakableOptionHandle()
            }
            field = value
        }

    var rarity: ItemRarity? = null
        set(value) {
            value?.let { options[RarityOptionHandle::class.java] = RarityOptionHandle(it) }
            field = value
        }

    var spawnerType: EntityType? = null
        set(value) {
            value?.let { options[SpawnerTypeOptionHandle::class.java] = SpawnerTypeOptionHandle(it) }
            field = value
        }

    var tooltipStyle: Key? = null
        set(value) {
            value?.let { options[TooltipStyleOptionHandle::class.java] = TooltipStyleOptionHandle(it) }
            field = value
        }

    var dyeColor: Color? = null
        set(value) {
            value?.let { options[DyeOptionHandle::class.java] = DyeOptionHandle(it) }
            field = value
        }

    val enchants = mutableMapOf<String, Int>()
    val flags = mutableSetOf<ItemFlag>()

    fun build(): StackedItemImpl {
        if (lore.isNotEmpty()) {
            options[LoreOptionHandle::class.java] = LoreOptionHandle(lore)
        }
        if (enchants.isNotEmpty()) {
            options[EnchantsOptionHandle::class.java] = EnchantsOptionHandle(enchants)
        }
        if (flags.isNotEmpty()) {
            options[FlagsOptionHandle::class.java] = FlagsOptionHandle(flags.toList())
        }

        return StackedItemImpl(
            item = baseStack,
            options = options.values,
            handler = ItemHandler.Impl
        )
    }
}

fun stackedItem(material: Material, builder: StackedItemBuilder.() -> Unit): StackedItemImpl {
    return StackedItemBuilder(ItemStack(material)).apply(builder).build()
}

fun ItemStack.toStackedBuilder(builder: StackedItemBuilder.() -> Unit): StackedItemImpl {
    return StackedItemBuilder(this.clone()).apply(builder).build()
}

fun Material.toStackedBuilder(builder: StackedItemBuilder.() -> Unit): StackedItemImpl {
    return StackedItemBuilder(ItemStack(this)).apply(builder).build()
}

@StackedDsl
class CustomModelDataBuilder {
    val colors = mutableListOf<Color>()
    val floats = mutableListOf<Float>()
    val flags = mutableListOf<Boolean>()
    val strings = mutableListOf<String>()

    fun build(): CustomModelDataOptionHandle {
        return CustomModelDataOptionHandle(
            colors.toList(),
            floats.toList(),
            flags.toList(),
            strings.toList()
        )
    }
}