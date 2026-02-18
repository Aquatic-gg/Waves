package gg.aquatic.execute.action.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.Action
import gg.aquatic.execute.Execute
import gg.aquatic.execute.action.type.PlayerAction
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import java.time.Duration

object TitleAction : PlayerAction() {

    override suspend fun execute(binder: Player, args: ArgumentContext<Player>) {
        val title: String = args.string("title") ?: ""
        val subtitle: String = args.string("subtitle") ?: ""
        val fadeIn: Int = args.int("fade-in") ?: 0
        val stay: Int = args.int("stay") ?: 0
        val fadeOut: Int = args.int("fade-out") ?: 0

        binder.showTitle(
            Title.title(
                Execute.miniMessage.deserialize(title),
                Execute.miniMessage.deserialize(subtitle),
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