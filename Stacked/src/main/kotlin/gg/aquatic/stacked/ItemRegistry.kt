package gg.aquatic.stacked

import gg.aquatic.kregistry.Registry
import gg.aquatic.stacked.event.StackedItemInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

fun StackedItem.register(
    namespace: String,
    id: String
): Boolean {
    return register(
        namespace, id,
        registerInteraction = false
    )
}

fun StackedItem.register(
    namespace: String,
    id: String,
    interactionHandler: (StackedItemInteractEvent) -> Unit
): Boolean {
    return register(namespace, id, interactionHandler, true)
}

private fun StackedItem.register(
    namespace: String, id: String,
    interactionHandler: (StackedItemInteractEvent) -> Unit = {}, registerInteraction: Boolean
): Boolean {
    val registryId = registryId()
    val item = getUnmodifiedItem()
    if (registryId != null && registryId != "$namespace:$id") return false

    item.editPersistentDataContainer {
        it.set(ItemHandler.NAMESPACE_KEY, PersistentDataType.STRING, "$namespace:$id")
    }

    Registry.update {
        replaceRegistry(
            StackedItem.ITEM_REGISTRY_KEY
        ) {
            register("$namespace:$id", this@register)
        }
    }

    if (registerInteraction) {
        ItemHandler.listenInteractions["$namespace:$id"] = interactionHandler
    }
    return true
}

fun StackedItem.setInteractionHandler(interactionHandler: (StackedItemInteractEvent) -> Unit): Boolean {
    val registryId = registryId() ?: return false
    ItemHandler.listenInteractions[registryId] = interactionHandler
    return true
}

fun StackedItem.removeInteractionHandler(): Boolean {
    val registryId = registryId() ?: return false
    ItemHandler.listenInteractions.remove(registryId)
    return true
}

fun StackedItem.unregister(): Boolean {
    val registryId = registryId() ?: return false
    val item = getUnmodifiedItem()
    item.editPersistentDataContainer {
        it.remove(ItemHandler.NAMESPACE_KEY)
    }

    var found = false
    Registry.update {
        replaceRegistry(
            StackedItem.ITEM_REGISTRY_KEY
        ) {
            if (unregister(registryId) != null) found = true
        }
    }
    if (!found) return false
    ItemHandler.listenInteractions.remove(registryId)
    return true
}

fun StackedItem.registryId(): String? {
    val pdc = getUnmodifiedItem().persistentDataContainer
    return pdc.get(ItemHandler.NAMESPACE_KEY, PersistentDataType.STRING)
}

fun ItemStack.toStacked(): StackedItem? {
    val pdc = persistentDataContainer
    val namespacedKey = ItemHandler.NAMESPACE_KEY
    if (!pdc.has(namespacedKey, PersistentDataType.STRING)) return null
    val id = pdc.get(namespacedKey, PersistentDataType.STRING) ?: return null

    return StackedItem.ITEMS[id]
}