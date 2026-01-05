package gg.aquatic.stacked

import gg.aquatic.stacked.option.AmountOptionHandle
import gg.aquatic.stacked.option.CustomModelDataLegacyOptionHandle
import gg.aquatic.stacked.option.CustomModelDataOptionHandle
import gg.aquatic.stacked.option.DamageOptionHandle
import gg.aquatic.stacked.option.DisplayNameOptionHandle
import gg.aquatic.stacked.option.DyeOptionHandle
import gg.aquatic.stacked.option.EnchantsOptionHandle
import gg.aquatic.stacked.option.FlagsOptionHandle
import gg.aquatic.stacked.option.ItemModelOptionHandle
import gg.aquatic.stacked.option.ItemOptionHandle
import gg.aquatic.stacked.option.LoreOptionHandle
import gg.aquatic.stacked.option.MaxDamageOptionHandle
import gg.aquatic.stacked.option.MaxStackSizeOptionHandle
import gg.aquatic.stacked.option.RarityOptionHandle
import gg.aquatic.stacked.option.SpawnerTypeOptionHandle
import gg.aquatic.stacked.option.TooltipStyleOptionHandle
import gg.aquatic.stacked.option.UnbreakableOptionHandle
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import kotlin.jvm.java

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
            value?.let { options[CustomModelDataLegacyOptionHandle::class.java] = CustomModelDataLegacyOptionHandle(it) }
            field = value
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

    fun build(): StackedItem {
        if (lore.isNotEmpty()) {
            options[LoreOptionHandle::class.java] = LoreOptionHandle(lore)
        }
        if (enchants.isNotEmpty()) {
            options[EnchantsOptionHandle::class.java] = EnchantsOptionHandle(enchants)
        }
        if (flags.isNotEmpty()) {
            options[FlagsOptionHandle::class.java] = FlagsOptionHandle(flags.toList())
        }

        return StackedItem(
            factoryId = null,
            internalId = null,
            item = baseStack,
            options = options.values
        )
    }
}

fun stackedItem(material: Material, builder: StackedItemBuilder.() -> Unit): StackedItem {
    return StackedItemBuilder(ItemStack(material)).apply(builder).build()
}

fun ItemStack.toStackedBuilder(builder: StackedItemBuilder.() -> Unit): StackedItem {
    return StackedItemBuilder(this.clone()).apply(builder).build()
}
