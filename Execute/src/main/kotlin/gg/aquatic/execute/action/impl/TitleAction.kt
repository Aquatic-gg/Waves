package gg.aquatic.execute.action.impl

import gg.aquatic.execute.Action
import gg.aquatic.execute.argument.ArgumentContext
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.minimessage.toMMComponent
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import java.time.Duration

object TitleAction : Action<Player> {

    override fun execute(binder: Player, args: ArgumentContext<Player>) {

        val title by args or ""
        val subtitle by args or ""
        val fadeIn by args id "fade-in" or 0
        val stay by args or 0
        val fadeOut by args id "fade-out" or 0

        binder.showTitle(
            Title.title(
                title.toMMComponent(),
                subtitle.toMMComponent(),
                Title.Times.times(
                    Duration.ofMillis((fadeIn * 50).toLong()),
                    Duration.ofMillis((stay * 50).toLong()),
                    Duration.ofMillis((fadeOut * 50).toLong())
                )
            )
        )
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("title", "", true),
        PrimitiveObjectArgument("subtitle", "", true),
        PrimitiveObjectArgument("fade-in", 0, true),
        PrimitiveObjectArgument("stay", 60, true),
        PrimitiveObjectArgument("fade-out", 0, true)
    )
}
