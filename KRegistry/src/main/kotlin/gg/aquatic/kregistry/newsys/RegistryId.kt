package gg.aquatic.kregistry.newsys

class RegistryId(
    val namespace: String,
    val key: String
) {
    companion object {
        fun fromString(string: String): RegistryId {
            val parts = string.split(':')
            if (parts.size != 2) throw IllegalArgumentException("Invalid registry ID format")
            return RegistryId(parts[0], parts[1])
        }
    }

    override fun toString(): String = "$namespace:$key"
}