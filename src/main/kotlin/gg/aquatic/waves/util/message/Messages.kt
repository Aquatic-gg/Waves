package gg.aquatic.waves.util.message

import gg.aquatic.waves.util.message.handler.MessageHandler
import java.util.concurrent.ConcurrentHashMap

object Messages {

    val registeredMessages = ConcurrentHashMap<String, () -> Message>()

    inline fun <reified T> injectMessages(namespace: String) where T : MessageHandler, T : Enum<T> {
        for (t in enumValues<T>()) {
            registeredMessages["$namespace:${t.name.lowercase()}"] = { t.message }
        }
    }

    operator fun get(key: String): Message? {
        return registeredMessages[key]?.invoke()
    }
}
