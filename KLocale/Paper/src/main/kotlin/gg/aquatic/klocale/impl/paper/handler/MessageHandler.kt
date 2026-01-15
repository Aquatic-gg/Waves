package gg.aquatic.klocale.impl.paper.handler

import gg.aquatic.klocale.LocaleManager
import gg.aquatic.klocale.message.Message
import java.util.*

interface MessageHandler<T: Message<T>> {

    fun message(language: String): T {
        return message(Locale.forLanguageTag(language))
    }
    fun message(locale: Locale): T
    val manager: LocaleManager<T>
}