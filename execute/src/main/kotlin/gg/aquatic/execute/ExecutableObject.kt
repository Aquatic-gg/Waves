package gg.aquatic.execute

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey
import gg.aquatic.kregistry.grouped.GroupedEntry
import gg.aquatic.kregistry.grouped.GroupedRegistry

interface ExecutableObject<A, B> : GroupedEntry<A> {

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

interface Action<A : Any> : ExecutableObject<A, Unit> {
    companion object {
        typealias ActionRegistry<T> = GroupedRegistry<String, T, Action<out T>>
        val REGISTRY_KEY = RegistryKey.grouped<String, Any, Action<*>>(
            RegistryId("aquatic", "actions")
        )

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> ActionRegistry<*>.getHierarchical(id: String, clazz: Class<T>): Action<T>? {
            return (this as GroupedRegistry<String, T, Action<T>>).getHierarchicalByClass(id, clazz)
        }

        val REGISTRY: ActionRegistry<*>
            get() {
                return Execute.bootstrapHolder[REGISTRY_KEY]
            }
    }
}

interface Condition<A: Any> : ExecutableObject<A, Boolean> {
    companion object {
        typealias ConditionRegistry<T> = GroupedRegistry<String, T, Condition<out T>>
        val REGISTRY_KEY = RegistryKey.grouped<String, Any, Condition<*>>(
            RegistryId("aquatic", "conditions")
        )

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> ConditionRegistry<*>.getHierarchical(id: String, clazz: Class<T>): Condition<T>? {
            return (this as GroupedRegistry<String, T, Condition<T>>).getHierarchicalByClass(id, clazz)
        }

        val REGISTRY: ConditionRegistry<*>
            get() {
                return Execute.bootstrapHolder.get(REGISTRY_KEY)
            }
    }
}

typealias ActionHandle<A> = ExecutableObjectHandle<A, Unit>