package gg.aquatic.kregistry.grouped

interface GroupedEntry<out Group> {
    val binder: Class<out Group>
}
