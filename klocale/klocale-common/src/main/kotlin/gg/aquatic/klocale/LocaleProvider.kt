package gg.aquatic.klocale

import gg.aquatic.klocale.message.Message

fun interface LocaleProvider<A : Message<A>> {

    suspend fun fetch(): Map<String, Map<String, A>>

}