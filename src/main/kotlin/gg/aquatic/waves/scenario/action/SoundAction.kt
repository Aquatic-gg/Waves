package gg.aquatic.waves.scenario.action

import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import org.bukkit.Bukkit
import org.bukkit.SoundCategory

@RegisterAction("play-sound")
class SoundAction : Action<Scenario> {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("sound","",true),
        PrimitiveObjectArgument("pitch",1f,false),
        PrimitiveObjectArgument("volume",100f,false),
        PrimitiveObjectArgument("category","AMBIENT",false)
    )

    override fun execute(binder: Scenario, args: ObjectArguments, textUpdater: (Scenario, String) -> String) {
        val sound = args.string("sound") { textUpdater(binder, it)} ?: return
        val pitch = args.float("pitch") { textUpdater(binder, it)} ?: return
        val volume = args.float("volume") { textUpdater(binder, it)} ?: return
        val category = args.string("category") { textUpdater(binder, it)} ?: return
        val soundCategory = SoundCategory.valueOf(category.uppercase())

        for (uuid in binder.audience.uuids) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            if (player.location.world != binder.baseLocation.world) continue
            if (player.location.distanceSquared(binder.baseLocation) > 2500) continue
            player.playSound(
                binder.baseLocation,
                sound,
                soundCategory,
                volume,
                pitch
            )
        }
        //binder.player.playSound(binder.player.location, sound, volume, pitch)
    }
}