package gg.aquatic.quickminimessage.tag.resolver

import gg.aquatic.quickminimessage.tag.MMTag
import gg.aquatic.quickminimessage.tag.MMTagContext
import java.util.*

fun interface MMTagResolver {
    fun resolve(name: String, args: List<String>, context: MMTagContext): MMTag?

    fun has(name: String): Boolean = true

    companion object {
        @JvmStatic
        fun empty(): MMTagResolver = EmptyResolver

        @JvmStatic
        fun resolver(name: String, tag: MMTag): MMTagResolver {
            return MapTagResolver(mapOf(normalizeName(name) to { _, _ -> tag }))
        }

        @JvmStatic
        fun resolver(name: String, handler: (List<String>, MMTagContext) -> MMTag?): MMTagResolver {
            return MapTagResolver(mapOf(normalizeName(name) to handler))
        }

        @JvmStatic
        fun resolver(names: Set<String>, handler: (List<String>, MMTagContext) -> MMTag?): MMTagResolver {
            if (names.isEmpty()) {
                return EmptyResolver
            }
            val mapped = HashMap<String, (List<String>, MMTagContext) -> MMTag?>(names.size)
            for (name in names) {
                mapped[normalizeName(name)] = handler
            }
            return MapTagResolver(mapped)
        }

        @JvmStatic
        fun resolver(vararg resolvers: MMTagResolver): MMTagResolver {
            if (resolvers.isEmpty()) {
                return EmptyResolver
            }
            if (resolvers.size == 1) {
                return resolvers[0]
            }
            val flattened = ArrayList<MMTagResolver>(resolvers.size)
            for (resolver in resolvers) {
                when (resolver) {
                    is CompositeTagResolver -> flattened.addAll(resolver.resolvers)
                    EmptyResolver -> Unit
                    else -> flattened.add(resolver)
                }
            }
            return when (flattened.size) {
                0 -> EmptyResolver
                1 -> flattened[0]
                else -> CompositeTagResolver(flattened)
            }
        }

        @JvmStatic
        fun builder(): Builder = Builder()
    }

    class Builder internal constructor() {
        private val handlers = HashMap<String, (List<String>, MMTagContext) -> MMTag?>(4)
        private val resolvers = ArrayList<MMTagResolver>(4)

        fun tag(name: String, tag: MMTag): Builder {
            handlers[normalizeName(name)] = { _, _ -> tag }
            return this
        }

        fun resolver(name: String, handler: (List<String>, MMTagContext) -> MMTag?): Builder {
            handlers[normalizeName(name)] = handler
            return this
        }

        fun resolver(resolver: MMTagResolver): Builder {
            if (resolver !== EmptyResolver) {
                resolvers.add(resolver)
            }
            return this
        }

        fun resolvers(vararg resolvers: MMTagResolver): Builder {
            for (resolver in resolvers) {
                resolver(resolver)
            }
            return this
        }

        fun build(): MMTagResolver {
            val collected = ArrayList<MMTagResolver>(resolvers.size + 1)
            if (handlers.isNotEmpty()) {
                collected.add(MapTagResolver(handlers.toMap()))
            }
            collected.addAll(resolvers)
            return MMTagResolver.resolver(*collected.toTypedArray())
        }
    }
}

private object EmptyResolver : MMTagResolver {
    override fun resolve(name: String, args: List<String>, context: MMTagContext): MMTag? = null
    override fun has(name: String): Boolean = false
}

private class MapTagResolver(
    private val handlers: Map<String, (List<String>, MMTagContext) -> MMTag?>
) : MMTagResolver {
    override fun resolve(name: String, args: List<String>, context: MMTagContext): MMTag? {
        return handlers[name]?.invoke(args, context)
    }

    override fun has(name: String): Boolean = handlers.containsKey(name)
}

private class CompositeTagResolver(
    internal val resolvers: List<MMTagResolver>
) : MMTagResolver {
    override fun resolve(name: String, args: List<String>, context: MMTagContext): MMTag? {
        for (resolver in resolvers) {
            if (!resolver.has(name)) {
                continue
            }
            val tag = resolver.resolve(name, args, context)
            if (tag != null) {
                return tag
            }
        }
        return null
    }

    override fun has(name: String): Boolean {
        for (resolver in resolvers) {
            if (resolver.has(name)) {
                return true
            }
        }
        return false
    }
}

private fun normalizeName(name: String): String = name.lowercase(Locale.ROOT)