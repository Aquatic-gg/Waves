package gg.aquatic.waves.util.bossbar

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.*

class AquaticBossBar(
    message: Component,
    color: BossBar.Color,
    overlay: BossBar.Overlay,
    flags: MutableSet<BossBar.Flag>,
    progress: Float
) {

    private val uuid: UUID = UUID.randomUUID()
    private val bossBar: BossBar = BossBar.bossBar(message, progress, color, overlay, flags)

    var message: Component = message
        set(value) {
            field = value
            bossBar.name(value)
        }
    var color: BossBar.Color = color
        set(value) {
            field = value
            bossBar.color(value)
        }

    var overlay: BossBar.Overlay = overlay
        set(value) {
            field = value
            bossBar.overlay(value)
        }

    private var flags: EnumSet<BossBar.Flag> = EnumSet.noneOf(BossBar.Flag::class.java).apply { addAll(flags) }
    fun flags(): Set<BossBar.Flag> = flags.toSet()
    fun addFlag(flag: BossBar.Flag) {
        flags += flag
        bossBar.flags(flags)
    }
    fun removeFlag(flag: BossBar.Flag) {
        flags -= flag
        bossBar.flags(flags)
    }

    fun setFlags(flags: Set<BossBar.Flag>) {
        this.flags = EnumSet.noneOf(BossBar.Flag::class.java).apply { addAll(flags) }
        bossBar.flags(flags)
    }

    var progress: Float = progress
        set(value) {
            field = value
            bossBar.progress(value)
        }

    fun addViewer(player: Player) {
        bossBar.addViewer(player)
    }

    fun removeViewer(player: Player) {
        bossBar.removeViewer(player)
    }

}