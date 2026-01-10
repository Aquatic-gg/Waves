package gg.aquatic.kmenu.inventory

import org.bukkit.inventory.MenuType

enum class InventoryType(slots: Int, val menuType: MenuType) {
    GENERIC9X1(9, MenuType.GENERIC_9X1),
    GENERIC9X2(18, MenuType.GENERIC_9X2),
    GENERIC9X3(27, MenuType.GENERIC_9X3),
    GENERIC9X4(36, MenuType.GENERIC_9X4),
    GENERIC9X5(45, MenuType.GENERIC_9X5),
    GENERIC9X6(54, MenuType.GENERIC_9X6),
    GENERIC3X3(9, MenuType.GENERIC_3X3),
    CRAFTER3X3(10, MenuType.CRAFTER_3X3),
    ANVIL(3, MenuType.ANVIL),
    BEACON(1,MenuType.BEACON),
    BLAST_FURNACE(3,MenuType.BLAST_FURNACE),
    BREWING_STAND(4,MenuType.BREWING_STAND),
    CRAFTING_TABLE(10,MenuType.CRAFTING),
    ENCHANTMENT_TABLE(2,MenuType.ENCHANTMENT),
    FURNACE(3,MenuType.FURNACE),
    GRINDSTONE(3,MenuType.GRINDSTONE),
    HOPPER(5,MenuType.HOPPER),
    LECTERN(0,MenuType.LECTERN),
    LOOM(4,MenuType.LOOM),
    VILLAGER(3,MenuType.MERCHANT),
    SHULKER_BOX(27,MenuType.SHULKER_BOX),
    SMITHING_TABLE(4,MenuType.SMITHING),
    SMOKER(3,MenuType.SMOKER),
    CARTOGRAPHY_TABLE(3,MenuType.CARTOGRAPHY_TABLE),
    STONECUTTER(2,MenuType.STONECUTTER)
    ;

    val size = slots
    val lastIndex = slots - 1

    fun id() = if (ordinal < 24) ordinal else 5
}