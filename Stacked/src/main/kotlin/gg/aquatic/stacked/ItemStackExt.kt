package gg.aquatic.stacked

import gg.aquatic.stacked.impl.StackedItemImpl
import org.bukkit.Material
import org.bukkit.block.CreatureSpawner
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.encode(): ByteArray {
    return ItemEncoder.encode(this)
}

fun ByteArray.decodeToItemStack(): ItemStack {
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

fun Material.toCustomItem(): StackedItemImpl {
    return ItemHandler.Impl.create(ItemStack(this), listOf())
}

fun ItemStack.modifyMeta(block: (ItemMeta) -> Unit): ItemStack {
    val meta = itemMeta ?: return this
    block(meta)
    itemMeta = meta
    return this
}