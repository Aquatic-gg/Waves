package gg.aquatic.waves.util.action.impl.discord

import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.task.AsyncCtx
import me.micartey.webhookly.DiscordWebhook

@RegisterAction("discord-webhook")
class DiscordWebhookAction : Action<Unit> {

    @Suppress("UNCHECKED_CAST")
    override fun execute(binder: Unit, args: ObjectArguments, textUpdater: (Unit, String) -> String) {
        val url = args.string("url") { str -> textUpdater(binder, str) } ?: return
        val content = args.string("content") { str -> textUpdater(binder, str) } ?: return
        val username = args.string("username") { str -> textUpdater(binder, str) } ?: return
        val avatarUrl = args.string("avatar-url") { str -> textUpdater(binder, str) } ?: return
        val tts = args.boolean("tts") { str -> textUpdater(binder, str) } ?: return
        val embeds = args.typed<Collection<DiscordEmbedArgument.WavesEmbedObject>>("embeds") { str -> textUpdater(binder, str) } ?: return

        AsyncCtx {
            val webhook = DiscordWebhook(url)
            webhook.setContent(content)
            webhook.setAvatarUrl(avatarUrl)
            webhook.setUsername(username)
            webhook.setTts(tts)
            webhook.embeds += embeds.map { embed -> embed.convert { textUpdater(binder, it) } }

            webhook.execute()
        }
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("url", "", true),
        PrimitiveObjectArgument("content", "", false),
        PrimitiveObjectArgument("username", "AquaticCrates", false),
        PrimitiveObjectArgument("avatar-url", "", false),
        PrimitiveObjectArgument("tts", false, required = false),
        DiscordEmbedArgument("embeds", ArrayList(), false)
    )
}