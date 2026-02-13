package gg.aquatic.execute

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.kregistry.*

interface ExecutableObject<A, B> {

    suspend fun execute(binder: A, args: ArgumentContext<A>): B
    val arguments: List<ObjectArgument<*>>

    fun arguments(builder: (ArgumentBuilder).() -> Unit): List<ObjectArgument<*>> {
        val argumentBuilder = ArgumentBuilder()
        builder(argumentBuilder)
        return argumentBuilder.build()
    }

    class ArgumentBuilder {
        private val arguments = mutableListOf<ObjectArgument<*>>()

        fun primitive(id: String, def: Any? = null, required: Boolean = false) {
            arguments += PrimitiveObjectArgument(id, def, required)
        }

        fun add(vararg arguments: ObjectArgument<*>) {
            this.arguments += arguments
        }

        fun build(): List<ObjectArgument<*>> {
            return arguments
        }
    }
}

interface Action<A>: ExecutableObject<A, Unit> {
    companion object {
        typealias ActionRegistry = TypedRegistry<String, Action<*>>
        val REGISTRY_KEY = RegistryKey<Class<*>, FrozenRegistry<String, Action<*>>>(
            RegistryId("aquatic","actions")
        )

        val REGISTRY: ActionRegistry
            get() {
                return Registry.get(REGISTRY_KEY)
            }
    }
}
interface Condition<A>: ExecutableObject<A, Boolean> {
    companion object {
        typealias ConditionRegistry = TypedRegistry<String, Condition<*>>
        val REGISTRY_KEY = RegistryKey<Class<*>, FrozenRegistry<String, Condition<*>>>(
            RegistryId("aquatic","conditions")
        )

        val REGISTRY: ConditionRegistry
            get() {
                return Registry.get(REGISTRY_KEY)
            }
    }
}

typealias ActionHandle<A> = ExecutableObjectHandle<A, Unit>