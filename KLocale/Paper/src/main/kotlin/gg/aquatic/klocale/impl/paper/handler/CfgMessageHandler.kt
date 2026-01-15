package gg.aquatic.klocale.impl.paper.handler

import gg.aquatic.klocale.message.Message
import java.util.*

interface CfgMessageHandler<T: Message<T>> : MessageHandler<T> {
    val path: String

    override fun message(locale: Locale): T {
        return manager.getOrThrow(locale, path)
    }
}