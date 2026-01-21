package gg.aquatic.kmenu.inventory

import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType

open class InventoryType(val size: Int, val menuType: MenuType) {

    val lastIndex = size - 1

    companion object {
        private val values = HashMap<String, InventoryType>()

        private fun <T : InventoryType> register(name: String, type: T): T {
            values[name.uppercase()] = type
            return type
        }

        val GENERIC9X1 = register("GENERIC9X1", InventoryType(9, MenuType.GENERIC_9X1))
        val GENERIC9X2 = register("GENERIC9X2", InventoryType(18, MenuType.GENERIC_9X2))
        val GENERIC9X3 = register("GENERIC9X3", InventoryType(27, MenuType.GENERIC_9X3))
        val GENERIC9X4 = register("GENERIC9X4", InventoryType(36, MenuType.GENERIC_9X4))
        val GENERIC9X5 = register("GENERIC9X5", InventoryType(45, MenuType.GENERIC_9X5))
        val GENERIC9X6 = register("GENERIC9X6", InventoryType(54, MenuType.GENERIC_9X6))
        val GENERIC3X3 = register("GENERIC3X3", InventoryType(9, MenuType.GENERIC_3X3))
        val CRAFTER3X3 = register("CRAFTER3X3", InventoryType(10, MenuType.CRAFTER_3X3))
        val BEACON = register("BEACON", InventoryType(1, MenuType.BEACON))
        val BLAST_FURNACE = register("BLAST_FURNACE", InventoryType(3, MenuType.BLAST_FURNACE))
        val BREWING_STAND = register("BREWING_STAND", InventoryType(4, MenuType.BREWING_STAND))
        val CRAFTING_TABLE = register("CRAFTING_TABLE", InventoryType(10, MenuType.CRAFTING))
        val ENCHANTMENT_TABLE = register("ENCHANTMENT_TABLE", InventoryType(2, MenuType.ENCHANTMENT))
        val FURNACE = register("FURNACE", InventoryType(3, MenuType.FURNACE))
        val GRINDSTONE = register("GRINDSTONE", InventoryType(3, MenuType.GRINDSTONE))
        val HOPPER = register("HOPPER", InventoryType(5, MenuType.HOPPER))
        val LECTERN = register("LECTERN", InventoryType(0, MenuType.LECTERN))
        val LOOM = register("LOOM", InventoryType(4, MenuType.LOOM))
        val VILLAGER = register("VILLAGER", InventoryType(3, MenuType.MERCHANT))
        val SHULKER_BOX = register("SHULKER_BOX", InventoryType(27, MenuType.SHULKER_BOX))
        val SMITHING_TABLE = register("SMITHING_TABLE", InventoryType(4, MenuType.SMITHING))
        val SMOKER = register("SMOKER", InventoryType(3, MenuType.SMOKER))
        val CARTOGRAPHY_TABLE = register("CARTOGRAPHY_TABLE", InventoryType(3, MenuType.CARTOGRAPHY_TABLE))
        val STONECUTTER = register("STONECUTTER", InventoryType(2, MenuType.STONECUTTER))

        val ANVIL get() = AnvilInventoryType()

        fun valueOf(name: String): InventoryType {
            val upperName = name.uppercase()
            if (upperName == "ANVIL") return ANVIL
            return values[upperName] ?: throw IllegalArgumentException("No InventoryType constant $name")
        }
    }
}

class AnvilInventoryType : InventoryType(3, MenuType.ANVIL) {
    var onRename: ((player: Player, name: String, inventory: PacketInventory) -> Unit)? = null

    fun onRename(block: (player: Player, name: String, inventory: PacketInventory) -> Unit): AnvilInventoryType {
        this.onRename = block
        return this
    }
}