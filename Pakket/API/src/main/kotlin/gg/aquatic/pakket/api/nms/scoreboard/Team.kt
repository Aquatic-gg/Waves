package gg.aquatic.pakket.api.nms.scoreboard

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.scoreboard.Team

class Team(
    val teamName: String,
    val prefix: Component,
    val suffix: Component,
    val nametagVisibility: Team.OptionStatus,
    val collisionRule: Team.Option,
    val nameColor: NamedTextColor,
)