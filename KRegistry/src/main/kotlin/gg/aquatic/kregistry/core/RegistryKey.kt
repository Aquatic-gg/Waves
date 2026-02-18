package gg.aquatic.kregistry.core

@JvmInline
value class RegistryKey<K, V>(
    val id: RegistryId
) {
    companion object {
        inline fun <reified T : Any> typedCollection(id: RegistryId): RegistryKey<Class<out T>, List<GenericTyped<out T>>> {
            return RegistryKey(id)
        }

        fun <Id, Group, Value> grouped(id: RegistryId): RegistryKey<Class<out Group>, Registry<Id, Value>> {
            return RegistryKey(id)
        }

        fun <K, V> fromString(namespacedKey: String): RegistryKey<K, V> {
            return RegistryKey(RegistryId.fromString(namespacedKey))
        }
    }
}
