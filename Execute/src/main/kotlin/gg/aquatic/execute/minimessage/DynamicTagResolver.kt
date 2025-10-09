package gg.aquatic.execute.minimessage

import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class DynamicTagResolver<T>(
    val name: String,
    val func: (T, ArgumentQueue, Context) -> Tag
) {

    fun create(binder: T): TagResolver {
        return TagResolver.builder().tag(name) { args, ctx ->
            func(binder, args, ctx)
        }.build()
    }

}