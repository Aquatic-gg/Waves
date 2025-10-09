package gg.aquatic.execute

import gg.aquatic.execute.argument.ArgumentContext
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.argument.impl.PrimitiveObjectArgument

interface ExecutableObject<A, B> {

    fun execute(binder: A, args: ArgumentContext<A>): B
    val arguments: List<ObjectArgument<*>>

    fun arguments(builder: (ArgumentBuilder<A, B>).() -> Unit): List<ObjectArgument<*>> {
        val argumentBuilder = ArgumentBuilder<A, B>()
        builder(argumentBuilder)
        return argumentBuilder.build()
    }

    class ArgumentBuilder<A, B> {
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

typealias Action<A> = ExecutableObject<A, Unit>
typealias Condition<A> = ExecutableObject<A, Boolean>