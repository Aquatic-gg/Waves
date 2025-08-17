package gg.aquatic.waves.util.action.impl.logical

import gg.aquatic.waves.registry.serializer.ActionSerializer
import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.chance.IChance
import gg.aquatic.waves.util.chance.randomItem
import gg.aquatic.waves.util.generic.ClassTransform
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import gg.aquatic.waves.util.getSectionList
import org.bukkit.configuration.ConfigurationSection

class RandomActionAction<T : Any>(
    clazz: Class<T>,
    classTransforms: Collection<ClassTransform<*, *>>,
) : SmartAction<T>(clazz, classTransforms as Collection<ClassTransform<T, *>>) {
    override fun execute(
        binder: T,
        args: ObjectArguments,
        textUpdater: (T, String) -> String,
    ) {
        val actions = args.any("actions") as? Collection<ChanceAction<T>> ?: return
        val chooseAmount = args.int("choose-amount") { str -> textUpdater(binder, str) } ?: return
        val chooseUnique = args.boolean("choose-unique") { str -> textUpdater(binder, str) } ?: return
        val actionsLeft = actions.toMutableList()
        for (i in 0..<chooseAmount) {
            val action = actionsLeft.randomItem() ?: break
            if (chooseUnique) {
                actionsLeft.remove(action)
            }
            action.action.execute(binder, textUpdater)
        }
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        ChanceActionsArgument("actions", listOf(), true, clazz, super.classTransforms),
        PrimitiveObjectArgument("choose-amount", 1, true),
        PrimitiveObjectArgument("choose-unique", false, required = false),
    )

    class ChanceActionsArgument<T : Any>(
        id: String, defaultValue: Collection<ChanceAction<T>>?, required: Boolean,
        val clazz: Class<T>,
        val transforms: Collection<ClassTransform<T, *>>,
    ) : AquaticObjectArgument<Collection<ChanceAction<T>>>(
        id, defaultValue,
        required,
    ) {
        override val serializer: AbstractObjectArgumentSerializer<Collection<ChanceAction<T>>?> =
            Serializer()

        override fun load(section: ConfigurationSection): Collection<ChanceAction<T>>? {
            return serializer.load(section, id)
        }

        inner class Serializer() :
            AbstractObjectArgumentSerializer<Collection<ChanceAction<T>>?>() {
            override fun load(
                section: ConfigurationSection,
                id: String,
            ): Collection<ChanceAction<T>> {
                val actions = mutableListOf<ChanceAction<T>>()
                for (section in section.getSectionList(id)) {
                    val chance = section.getDouble("chance")
                    val action = ActionSerializer.fromSection(clazz, section, *transforms.toTypedArray()) ?: continue
                    actions.add(ChanceAction(chance, action))
                }
                return actions
            }
        }
    }

    class ChanceAction<T>(
        override val chance: Double,
        val action: ConfiguredExecutableObject<T, Unit>
    ): IChance
}