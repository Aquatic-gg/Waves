package gg.aquatic.waves.item.option

import net.kyori.adventure.key.Key

enum class ItemOptions(val key: Key) {
    AMOUNT(AmountOptionHandle.key),
    CUSTOM_MODEL_DATA_LEGACY(CustomModelDataLegacyOptionHandle.key),
    CUSTOM_MODEL_DATA(CustomModelDataOptionHandle.key),
    DAMAGE(DamageOptionHandle.key),
    DISPLAY_NAME(DisplayNameOptionHandle.key),
    DYE(DyeOptionHandle.key),
    ENCHANTS(EnchantsOptionHandle.key),
    FLAGS(FlagsOptionHandle.key),
    ITEM_MODEL(ItemModelOptionHandle.key),
    LORE(LoreOptionHandle.key),
    MAX_DAMAGE(MaxDamageOptionHandle.key),
    MAX_STACK_SIZE(MaxStackSizeOptionHandle.key),
    RARITY(RarityOptionHandle.key),
    SPAWNER_TYPE(SpawnerTypeOptionHandle.key),
    TOOLTIP_STYLE(TooltipStyleOptionHandle.key),
    UNBREAKABLE(UnbreakableOptionHandle.key)
}