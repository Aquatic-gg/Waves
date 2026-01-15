package gg.aquatic.klocale.impl.paper.handler

import gg.aquatic.klocale.message.Message

interface CfgMessageHandler<T: Message<T>> : MessageHandler<T> {
    val path: String
}