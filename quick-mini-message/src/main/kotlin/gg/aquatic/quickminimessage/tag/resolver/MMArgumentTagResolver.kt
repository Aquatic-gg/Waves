package gg.aquatic.quickminimessage.tag.resolver

import gg.aquatic.quickminimessage.tag.MMTag
import gg.aquatic.quickminimessage.tag.MMTagContext

class MMArgumentTagResolver internal constructor(
    private val arguments: List<MMTag>,
    private val delegate: MMTagResolver
) : MMTagResolver {
    override fun resolve(name: String, args: List<String>, context: MMTagContext): MMTag? {
        if (name == "argument" || name == "arg") {
            val index = args.firstOrNull()?.toIntOrNull() ?: return null
            if (index < 0 || index >= arguments.size) {
                return null
            }
            return arguments[index]
        }
        if (!delegate.has(name)) {
            return null
        }
        return delegate.resolve(name, args, context)
    }

    override fun has(name: String): Boolean {
        return name == "argument" || name == "arg" || delegate.has(name)
    }
}