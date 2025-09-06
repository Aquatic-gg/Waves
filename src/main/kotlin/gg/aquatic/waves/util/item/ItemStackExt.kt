package gg.aquatic.waves.util.item

import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.item.ItemHandler
import gg.aquatic.waves.registry.serializer.ItemSerializer
import org.bukkit.Material
import org.bukkit.block.CreatureSpawner
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.encode(): String {
    return ItemEncoder.encode(this)
}

fun String.decodeToItemStack(): ItemStack {
    return ItemEncoder.decode(this)
}


fun ItemStack.setSpawnerType(type: EntityType) {
    val meta = itemMeta ?: return
    meta.setSpawnerType(type)
    itemMeta = meta
}

fun ItemMeta.setSpawnerType(type: EntityType) {
    if (this !is BlockStateMeta) return
    val blockState = this.blockState as? CreatureSpawner ?: return
    blockState.spawnedType = type
    this.blockState = blockState
}

fun Material.toCustomItem(): AquaticItem {
    return ItemHandler.create(null,ItemStack(this), listOf())
}

fun ItemStack.modifyMeta(block: (ItemMeta) -> Unit) {
    val meta = itemMeta ?: return
    block(meta)
    itemMeta = meta
}