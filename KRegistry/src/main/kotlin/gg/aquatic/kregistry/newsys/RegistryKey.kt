package gg.aquatic.kregistry.newsys

@JvmInline
value class RegistryKey<K, V>(
    val id: RegistryId
) {
    companion object {
        fun <K, V> fromString(): RegistryKey<K, V> {
            return RegistryKey(RegistryId.fromString("example"))
        }
    }
}