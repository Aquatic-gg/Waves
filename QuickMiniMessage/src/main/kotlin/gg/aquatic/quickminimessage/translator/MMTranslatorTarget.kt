package gg.aquatic.quickminimessage.translator

import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.VirtualComponentRenderer

class MMTranslatorTarget internal constructor(
    val pointered: Pointered
) : VirtualComponentRenderer<Void> {
    override fun apply(context: Void): ComponentLike? = null
}