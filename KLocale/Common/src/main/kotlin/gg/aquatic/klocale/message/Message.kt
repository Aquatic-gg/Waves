package gg.aquatic.klocale.message

interface Message<A : Message<A>> {

    fun replace(placeholder: String, replacement: String): A

    fun replace(updater: (String) -> String): A
}