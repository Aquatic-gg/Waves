package gg.aquatic.waves.util.action.impl.logical

import gg.aquatic.waves.registry.serializer.ActionSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.chance.IChance
import gg.aquatic.waves.util.chance.randomItem
import gg.aquatic.waves.util.generic.ClassTransform
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import gg.aquatic.waves.util.generic.ExecutableObject
import gg.aquatic.waves.util.getSectionList

class ConfiguredRandomAction<T>(
    val actions: List<ChanceAction<T>>,
    val chooseAmount: Int,
    val chooseUnique: Boolean,
) : ConfiguredExecutableObject<T, Unit>(object : ExecutableObject<T, Unit> {
    override fun execute(binder: T, args: ObjectArguments, textUpdater: (T, String) -> String) {
        val actionsLeft = actions.toMutableList()
        for (i in 0..<chooseAmount) {
            val action = actionsLeft.randomItem() ?: break
            if (chooseUnique) {
                actionsLeft.remove(action)
            }
            action.action.execute(binder, textUpdater)
        }
    }

    override val arguments: List<AquaticObjectArgument<*>> = emptyList()
}, ObjectArguments(hashMapOf())) {

    class ChanceAction<T>(
        override val chance: Double,
        val action: ConfiguredExecutableObject<T, Unit>,
    ) : IChance

    companion object {
        fun <T: Any> fromSection(clazz: Class<T>,section: org.bukkit.configuration.ConfigurationSection, vararg classTransforms: ClassTransform<T,*>): ConfiguredRandomAction<T>? {
            val actions = ArrayList<ChanceAction<T>>()
            val actionSections = section.getSectionList("actions")
            for (actionSection in actionSections) {
                val action = ActionSerializer.fromSection(clazz, actionSection, *classTransforms) ?: continue
                val chance = actionSection.getDouble("chance")
                actions += ChanceAction(chance, action)
            }
            val chooseAmount = section.getInt("choose-amount")
            val chooseUnique = section.getBoolean("choose-unique")

            return ConfiguredRandomAction(actions, chooseAmount, chooseUnique)
        }
    }
}