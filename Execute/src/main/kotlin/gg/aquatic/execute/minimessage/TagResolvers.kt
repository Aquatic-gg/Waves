package gg.aquatic.execute.minimessage

import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.iterator

object TagResolvers {

    val registered = ConcurrentHashMap<Class<*>, MutableCollection<DynamicTagResolver<*>>>()

    fun <T> register(binder: Class<T>, name: String, resolver: (T, ArgumentQueue, Context) -> Tag) {
        registered.computeIfAbsent(binder) { mutableListOf() }.add(DynamicTagResolver(name, resolver))
    }

    inline fun <reified T> resolverFor(binder: T, vararg transforms: Transform<T, *>): TagResolver {
        return resolverFor(T::class.java, binder, *transforms)
    }

    fun <T> resolverFor(clazz: Class<T>, binder: T, vararg transforms: Transform<T, *>): TagResolver {
        val resolvers = ArrayList<TagResolver>()

        val original = getRegisteredFor(clazz)
        for (resolver in original) {
            resolvers.add((resolver as DynamicTagResolver<T>).create(binder))
        }

        for (transform in transforms) {
            resolvers += transform.generate(binder)
        }

        return TagResolver.resolver(resolvers)
    }

    private fun getRegisteredFor(type: Class<*>): ArrayList<DynamicTagResolver<*>> {
        val resolvers = ArrayList<DynamicTagResolver<*>>()
        for ((clazz, typeResolvers) in registered) {
            if (type == clazz || clazz.isAssignableFrom(type)) {
                resolvers += typeResolvers
            }
        }
        return resolvers
    }

    class Transform<A, B>(
        val transformToClass: Class<B>,
        val func: (A) -> B
    ) {

        fun generate(value: A): List<TagResolver> {
            val generated = ArrayList<TagResolver>()
            val originals = getRegisteredFor(transformToClass)

            for (resolver in originals) {
                (resolver as DynamicTagResolver<B>).create(func(value))
            }
            return generated
        }
    }
}