package gg.aquatic.stacked.option

import net.kyori.adventure.key.Key

enum class ItemOptions(val key: Key) {
    AMOUNT(AmountOptionHandle.Companion.key),
    CUSTOM_MODEL_DATA_LEGACY(CustomModelDataLegacyOptionHandle.key),
    CUSTOM_MODEL_DATA(CustomModelDataOptionHandle.key),
    DAMAGE(DamageOptionHandle.Companion.key),
    DISPLAY_NAME(DisplayNameOptionHandle.key),
    DYE(DyeOptionHandle.Companion.key),
    ENCHANTS(EnchantsOptionHandle.Companion.key),
    FLAGS(FlagsOptionHandle.Companion.key),
    HIDE_TOOLTIP(HideTooltipOptionHandle.Companion.key),
    ITEM_MODEL(ItemModelOptionHandle.key),
    LORE(LoreOptionHandle.Companion.key),
    MAX_DAMAGE(MaxDamageOptionHandle.Companion.key),
    MAX_STACK_SIZE(MaxStackSizeOptionHandle.Companion.key),
    RARITY(RarityOptionHandle.Companion.key),
    SPAWNER_TYPE(SpawnerTypeOptionHandle.Companion.key),
    TOOLTIP_STYLE(TooltipStyleOptionHandle.Companion.key),
    UNBREAKABLE(UnbreakableOptionHandle.Companion.key)
}