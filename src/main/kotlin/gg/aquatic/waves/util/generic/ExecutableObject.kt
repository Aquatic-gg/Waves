package gg.aquatic.waves.util.generic

import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.BlockArgument
import gg.aquatic.waves.util.argument.impl.ItemObjectArgument
import gg.aquatic.waves.util.argument.impl.MessageArgument
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.block.AquaticBlock
import gg.aquatic.waves.util.message.Message

interface ExecutableObject<A, B> {

    fun execute(binder: A, args: ObjectArguments, textUpdater: (A, String) -> String): B
    val arguments: List<AquaticObjectArgument<*>>

    fun arguments(builder: (ArgumentBuilder<A,B>).() -> Unit): List<AquaticObjectArgument<*>> {
        val argumentBuilder = ArgumentBuilder<A,B>()
        builder(argumentBuilder)
        return argumentBuilder.build()
    }

    class ArgumentBuilder<A, B> {
        private val arguments = mutableListOf<AquaticObjectArgument<*>>()

        fun primitive(id: String, def: Any? = null, required: Boolean = false) {
            arguments += PrimitiveObjectArgument(id, def, required)
        }
        fun aquaticItem(id: String, def: AquaticItem? = null, required: Boolean = false) {
            arguments += ItemObjectArgument(id, def, required)
        }
        fun aquaticBlock(id: String, def: AquaticBlock? = null, required: Boolean = false) {
            arguments += BlockArgument(id, def, required)
        }
        fun message(id: String, def: Message? = null, required: Boolean = false) {
            arguments += MessageArgument(id, def, required)
        }

        fun add(vararg arguments: AquaticObjectArgument<*>) {
            this.arguments += arguments
        }

        fun build(): List<AquaticObjectArgument<*>> {
            return arguments
        }
    }
}

typealias Action<A> = ExecutableObject<A, Unit>
typealias Condition<A> = ExecutableObject<A, Boolean>