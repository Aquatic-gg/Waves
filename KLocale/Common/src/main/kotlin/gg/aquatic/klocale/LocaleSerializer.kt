package gg.aquatic.klocale

import gg.aquatic.klocale.message.Message

fun interface LocaleSerializer<A, B : Message<B>> {

    fun parse(data: A): Map<String,Map<String, B>>?

}