package gg.aquatic.quickminimessage.argument

import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.VirtualComponentRenderer

class MMTranslatorArgument<T> internal constructor(
    val name: String,
    val data: T
) : VirtualComponentRenderer<Void> {
    override fun apply(context: Void): ComponentLike? {
        return data as? ComponentLike
    }
}