package gg.aquatic.stacked

import gg.aquatic.kregistry.Registry
import gg.aquatic.stacked.event.StackedItemInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

fun <T: StackedItem<T>> StackedItem<T>.register(
    namespace: String,
    id: String
): Boolean {
    return register(
        namespace, id,
        registerInteraction = false
    )
}

fun <T: StackedItem<T>> StackedItem<T>.register(
    namespace: String,
    id: String,
    interactionHandler: (StackedItemInteractEvent) -> Unit
): Boolean {
    return register(namespace, id, interactionHandler, true)
}

private fun <T: StackedItem<T>> StackedItem<T>.register(
    namespace: String, id: String,
    interactionHandler: (StackedItemInteractEvent) -> Unit = {}, registerInteraction: Boolean
): Boolean {
    val registryId = registryId()
    val item = getBaseItem()

    if (registryId != null && registryId != "${handler.id}:$namespace:$id") return false

    item.editPersistentDataContainer {
        it.set(ItemHandler.NAMESPACE_KEY, PersistentDataType.STRING, "${handler.id}$namespace:$id")
    }

    Registry.update {
        replaceRegistry(
            StackedItem.ITEM_REGISTRY_KEY
        ) {
            register("${handler.id}:$namespace:$id", this@register)
        }
    }

    if (registerInteraction) {
        handler.listenInteractions["$namespace:$id"] = interactionHandler
    }
    return true
}

fun <T: StackedItem<T>> StackedItem<T>.setInteractionHandler(interactionHandler: (StackedItemInteractEvent) -> Unit): Boolean {
    val registryId = registryId() ?: return false
    handler.listenInteractions[registryId] = interactionHandler
    return true
}

fun <T: StackedItem<T>> StackedItem<T>.removeInteractionHandler(): Boolean {
    val registryId = registryId() ?: return false
    handler.listenInteractions.remove(registryId)
    return true
}

fun <T: StackedItem<T>> StackedItem<T>.unregister(): Boolean {
    val registryId = registryId() ?: return false
    val item = getBaseItem()
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
    handler.listenInteractions.remove(registryId)
    return true
}

fun <T: StackedItem<T>> StackedItem<T>.registryId(): String? {
    val pdc = getBaseItem().persistentDataContainer
    return pdc.get(ItemHandler.NAMESPACE_KEY, PersistentDataType.STRING)
}

fun ItemStack.toStacked(): StackedItem<*>? {
    val pdc = persistentDataContainer
    val namespacedKey = ItemHandler.NAMESPACE_KEY
    if (!pdc.has(namespacedKey, PersistentDataType.STRING)) return null
    val id = pdc.get(namespacedKey, PersistentDataType.STRING) ?: return null

    return StackedItem.ITEMS[id]
}