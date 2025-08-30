package gg.aquatic.waves.scenario.action.potion

import gg.aquatic.waves.scenario.PlayerScenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.generic.Action
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.potion.PotionEffectType

@RegisterAction("remove-potion-effects")
class ClearPotionEffectsAction: Action<PlayerScenario> {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PotionsArgument("potions", listOf(), true)
    )

    override fun execute(
        binder: PlayerScenario,
        args: ObjectArguments,
        textUpdater: (PlayerScenario, String) -> String
    ) {
        val potions = args.typed<List<PotionEffectType>>("potions") ?: listOf()
        for (type in potions) {
            binder.player.removePotionEffect(type)
        }
    }

    class PotionsArgument(
        id: String,
        defaultValue: List<PotionEffectType>?, required: Boolean, aliases: Collection<String> = listOf()
    ) : AquaticObjectArgument<List<PotionEffectType>>(id, defaultValue, required, aliases) {
        override val serializer: AbstractObjectArgumentSerializer<List<PotionEffectType>?> = Companion

        companion object : AbstractObjectArgumentSerializer<List<PotionEffectType>?>() {
            override fun load(section: ConfigurationSection, id: String): List<PotionEffectType> {
                val list = mutableListOf<PotionEffectType>()
                for (s in section.getStringList(id)) {
                    list += Registry.EFFECT.get(NamespacedKey.minecraft(s.lowercase())) ?: continue
                }
                return list
            }

        }

    }
}