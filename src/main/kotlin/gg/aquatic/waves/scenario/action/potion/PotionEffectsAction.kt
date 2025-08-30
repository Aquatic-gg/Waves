package gg.aquatic.waves.scenario.action.potion

import gg.aquatic.waves.scenario.PlayerScenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.getSectionList
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.collections.iterator

@RegisterAction("add-potion-effects")
class PotionEffectsAction : Action<PlayerScenario> {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PotionsArgument("potions", mapOf(), true)
    )

    override fun execute(
        binder: PlayerScenario,
        args: ObjectArguments,
        textUpdater: (PlayerScenario, String) -> String
    ) {
        val potions = args.typed<Map<PotionEffectType, Pair<Int, Int>>>("potions") ?: mapOf()
        for ((type, pair) in potions) {
            val (duration, amplifier) = pair
            binder.player.addPotionEffect(PotionEffect(type, duration, amplifier, false, false, false))
            //binder.player.addPotionEffect(type, duration, amplifier)
        }
    }

    class PotionsArgument(
        id: String,
        defaultValue: Map<PotionEffectType, Pair<Int, Int>>?, required: Boolean
    ) : AquaticObjectArgument<Map<PotionEffectType, Pair<Int, Int>>>(id, defaultValue, required) {
        override val serializer: AbstractObjectArgumentSerializer<Map<PotionEffectType, Pair<Int, Int>>?> = Companion

        override fun load(section: ConfigurationSection): Map<PotionEffectType, Pair<Int, Int>>? {
            return serializer.load(section, id)
        }

        companion object : AbstractObjectArgumentSerializer<Map<PotionEffectType, Pair<Int, Int>>?>() {
            override fun load(section: ConfigurationSection, id: String): Map<PotionEffectType, Pair<Int, Int>>? {
                val map = mutableMapOf<PotionEffectType, Pair<Int, Int>>()
                for (configurationSection in section.getSectionList(id)) {

                    val type = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(configurationSection.getString("potion") ?: continue)) ?: continue
                    val duration = configurationSection.getInt("duration")
                    val amplifier = configurationSection.getInt("amplifier")
                    map += type to (duration to amplifier)
                }
                return map
            }

        }

    }

}