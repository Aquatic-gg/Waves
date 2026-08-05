package gg.aquatic.stacked

import gg.aquatic.kregistry.bootstrap.RegistryContributionBuilder
import gg.aquatic.stacked.event.StackedItemInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

fun <T: StackedItem<T>> StackedItem<T>.register(
    registryBuilder: RegistryContributionBuilder<String, StackedItem<*>>,
    namespace: String,
    id: String
): Boolean {
    return register(
        registryBuilder, namespace, id,
        registerInteraction = false
    )
}

fun <T: StackedItem<T>> StackedItem<T>.register(
    registryBuilder: RegistryContributionBuilder<String, StackedItem<*>>,
    namespace: String,
    id: String,
    interactionHandler: (StackedItemInteractEvent) -> Unit
): Boolean {
    return register(registryBuilder, namespace, id, interactionHandler, true)
}

private fun <T: StackedItem<T>> StackedItem<T>.register(
    registryBuilder: RegistryContributionBuilder<String, StackedItem<*>>,
    namespace: String, id: String,
    interactionHandler: (StackedItemInteractEvent) -> Unit = {}, registerInteraction: Boolean,
): Boolean {
    val fullId = "${handler.id}:$namespace:$id"
    val registryId = registryId()
    val item = getBaseItem()

    if (registryId != null && registryId != fullId) return false

    item.editPersistentDataContainer {
        it.set(ItemHandler.NAMESPACE_KEY, PersistentDataType.STRING, fullId)
    }

    registryBuilder.add(fullId, this@register)

    if (registerInteraction) {
        handler.listenInteractions[fullId] = interactionHandler
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

fun <T: StackedItem<T>> StackedItem<T>.registryId(): String? {
    val pdc = getBaseItem().persistentDataContainer
    return pdc.get(ItemHandler.NAMESPACE_KEY, PersistentDataType.STRING)
}

fun ItemStack.toStacked(): StackedItem<*>? {
    val pdc = persistentDataContainer
    val namespacedKey = ItemHandler.NAMESPACE_KEY
    if (!pdc.has(namespacedKey, PersistentDataType.STRING)) {
        return null
    }
    val id = pdc.get(namespacedKey, PersistentDataType.STRING) ?: return null
    return StackedItem.ITEMS[id]
}
