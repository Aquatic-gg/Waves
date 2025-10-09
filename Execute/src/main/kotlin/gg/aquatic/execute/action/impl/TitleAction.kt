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

        val title: String? by args
        val subtitle: String? by args
        val fadeIn = args.int("fade-in") ?: 0
        val stay: Int? by args
        val fadeOut = args.int("fade-out") ?: 0

        binder.showTitle(
            Title.title(
                (title ?: "").toMMComponent(),
                (subtitle ?: "").toMMComponent(),
                Title.Times.times(
                    Duration.ofMillis((fadeIn * 50).toLong()),
                    Duration.ofMillis(((stay ?: 0) * 50).toLong()),
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